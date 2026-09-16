package com.shopsphere.order.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order event published to RabbitMQ orders.exchange.
 * Consumed by notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;   // ORDER_CREATED, ORDER_CONFIRMED, ORDER_CANCELLED, PAYMENT_SUCCESS, PAYMENT_FAILED
    private UUID orderId;
    private String userId;
    private String userEmail;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private LocalDateTime occurredAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, FAILED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private UUID productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
    }
}
