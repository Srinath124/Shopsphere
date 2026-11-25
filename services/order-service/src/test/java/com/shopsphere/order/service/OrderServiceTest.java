package com.shopsphere.order.service;

import com.shopsphere.order.client.InventoryClient;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.dto.OrderItemRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.messaging.OrderEventPublisher;
import com.shopsphere.order.repository.OrderRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    private OrderService orderService;

    private static final String USER_ID = "user-abc";
    private static final String IDEMPOTENCY_KEY = "idem-key-001";

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, inventoryClient, eventPublisher, new SimpleMeterRegistry()
        );
    }

    @Test
    @DisplayName("createOrder - returns existing order for duplicate idempotency key")
    void createOrder_duplicateIdempotencyKey_returnsExisting() {
        Order existingOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("99.99"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(orderRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .willReturn(Optional.of(existingOrder));

        CreateOrderRequest request = buildOrderRequest();

        OrderResponse result = orderService.createOrder(USER_ID, request, IDEMPOTENCY_KEY);

        assertThat(result.getId()).isEqualTo(existingOrder.getId());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        // Inventory should NOT be called for a duplicate request
        then(inventoryClient).should(never()).reserveStock(anyString(), anyInt());
        then(orderRepository).should(never()).save(any(Order.class));
    }

    @Test
    @DisplayName("getOrderById - throws OrderNotFoundException for unknown order")
    void getOrderById_unknownId_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        given(orderRepository.findByIdAndUserId(id, USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(id, USER_ID))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("getOrdersByUser - returns empty list when user has no orders")
    void getOrdersByUser_noOrders_returnsEmptyList() {
        given(orderRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).willReturn(List.of());

        List<OrderResponse> result = orderService.getUserOrders(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getOrdersByUser - returns mapped orders for user")
    void getOrdersByUser_withOrders_returnsMappedList() {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("49.99"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(orderRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).willReturn(List.of(order));

        List<OrderResponse> result = orderService.getUserOrders(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CreateOrderRequest buildOrderRequest() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("PROD-001");
        item.setProductName("Test Product");
        item.setUnitPrice(new BigDecimal("49.99"));
        item.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));
        request.setShippingAddress("123 Test Street, Testville");
        return request;
    }
}
