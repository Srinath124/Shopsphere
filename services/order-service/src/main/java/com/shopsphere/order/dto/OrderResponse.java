package com.shopsphere.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private String idempotencyKey;
    private String notes;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
