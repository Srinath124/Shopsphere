package com.shopsphere.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Product name is required")
    private String productName;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
