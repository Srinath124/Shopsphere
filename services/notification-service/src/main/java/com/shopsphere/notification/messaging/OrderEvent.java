package com.shopsphere.notification.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order event received from RabbitMQ orders.exchange.
 * Must match the OrderEvent structure published by order-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;
    private UUID orderId;
    private String userId;
    private String userEmail;
    private String orderStatus;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private LocalDateTime occurredAt;

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
