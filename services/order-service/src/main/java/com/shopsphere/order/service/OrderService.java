package com.shopsphere.order.service;

import com.shopsphere.order.client.InventoryClient;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderItem;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.dto.*;
import com.shopsphere.order.exception.InventoryServiceException;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.messaging.OrderEvent;
import com.shopsphere.order.messaging.OrderEventPublisher;
import com.shopsphere.order.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order creation workflow:
 * 1. Check idempotency key — return existing order if duplicate
 * 2. Create order in PENDING state
 * 3. Reserve inventory for each item
 * 4. Simulate payment (always succeeds in this demo)
 * 5. Mark order as CONFIRMED
 * 6. Deduct inventory
 * 7. Publish ORDER_CONFIRMED event to RabbitMQ
 *
 * On failure:
 * - Release any reserved inventory
 * - Mark order as FAILED
 * - Publish PAYMENT_FAILED event
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderEventPublisher eventPublisher;
    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;

    public OrderService(OrderRepository orderRepository,
                        InventoryClient inventoryClient,
                        OrderEventPublisher eventPublisher,
                        MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.eventPublisher = eventPublisher;
        this.orderCreatedCounter = Counter.builder("shopsphere.orders.created")
                .description("Number of orders successfully created")
                .register(meterRegistry);
        this.orderFailedCounter = Counter.builder("shopsphere.orders.failed")
                .description("Number of failed order attempts")
                .register(meterRegistry);
    }

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request, String idempotencyKey) {
        // Step 1: Idempotency check
        if (idempotencyKey != null) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent order request — returning existing order {}", existing.get().getId());
                return mapToResponse(existing.get());
            }
        }

        // Step 2: Calculate total
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 3: Create PENDING order
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .idempotencyKey(idempotencyKey)
                .notes(request.getNotes())
                .build();

        request.getItems().forEach(itemReq -> {
            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .unitPrice(itemReq.getUnitPrice())
                    .quantity(itemReq.getQuantity())
                    .build();
            order.addItem(item);
        });

        Order savedOrder = orderRepository.save(order);
        log.info("Created PENDING order {}", savedOrder.getId());

        // Step 4: Reserve inventory for each item
        List<OrderItem> reservedItems = new java.util.ArrayList<>();
        try {
            for (OrderItem item : savedOrder.getItems()) {
                inventoryClient.reserveStock(item.getProductId(), item.getQuantity());
                reservedItems.add(item);
            }
        } catch (InventoryServiceException e) {
            // Rollback: release any partially reserved items
            for (OrderItem reservedItem : reservedItems) {
                inventoryClient.releaseStock(reservedItem.getProductId(), reservedItem.getQuantity());
            }
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            orderFailedCounter.increment();
            log.error("Order {} failed: insufficient inventory", savedOrder.getId());
            publishFailureEvent(savedOrder, "PAYMENT_FAILED");
            throw e;
        }

        // Step 5: Simulate payment (always succeeds in demo)
        boolean paymentSuccess = simulatePayment(savedOrder);

        if (!paymentSuccess) {
            // Release inventory and fail order
            for (OrderItem item : savedOrder.getItems()) {
                inventoryClient.releaseStock(item.getProductId(), item.getQuantity());
            }
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            orderFailedCounter.increment();
            log.error("Order {} failed: payment rejected", savedOrder.getId());
            publishFailureEvent(savedOrder, "PAYMENT_FAILED");
            throw new RuntimeException("Payment failed for order: " + savedOrder.getId());
        }

        // Step 6: Confirm order
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(savedOrder);

        // Step 7: Deduct inventory (reserved → confirmed)
        for (OrderItem item : confirmedOrder.getItems()) {
            inventoryClient.deductStock(item.getProductId(), item.getQuantity());
        }

        // Step 8: Publish success event
        orderCreatedCounter.increment();
        publishSuccessEvent(confirmedOrder);
        log.info("Order {} confirmed successfully", confirmedOrder.getId());

        return mapToResponse(confirmedOrder);
    }

    public List<OrderResponse> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public OrderResponse getOrderById(UUID orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        // Release inventory
        for (OrderItem item : order.getItems()) {
            inventoryClient.releaseStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);
        publishCancelledEvent(cancelled);
        log.info("Order {} cancelled by user {}", orderId, userId);
        return mapToResponse(cancelled);
    }

    private boolean simulatePayment(Order order) {
        // Simulated payment — always succeeds in demo
        // In production: call payment gateway here
        log.info("Payment simulation for order {} amount {}: SUCCESS", order.getId(), order.getTotalAmount());
        return true;
    }

    private void publishSuccessEvent(Order order) {
        OrderEvent event = buildEvent(order, "ORDER_CONFIRMED", OrderEvent.OrderStatus.CONFIRMED);
        eventPublisher.publishOrderConfirmed(event);
        eventPublisher.publishPaymentSuccess(event);
    }

    private void publishFailureEvent(Order order, String eventType) {
        OrderEvent event = buildEvent(order, eventType, OrderEvent.OrderStatus.FAILED);
        eventPublisher.publishPaymentFailed(event);
    }

    private void publishCancelledEvent(Order order) {
        OrderEvent event = buildEvent(order, "ORDER_CANCELLED", OrderEvent.OrderStatus.CANCELLED);
        eventPublisher.publishOrderCancelled(event);
    }

    private OrderEvent buildEvent(Order order, String eventType, OrderEvent.OrderStatus status) {
        List<OrderEvent.OrderItemEvent> itemEvents = order.getItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return OrderEvent.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .userId(order.getUserId())
                .orderStatus(status)
                .totalAmount(order.getTotalAmount())
                .items(itemEvents)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .idempotencyKey(order.getIdempotencyKey())
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
