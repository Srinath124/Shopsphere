package com.shopsphere.cart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddItemRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity must not exceed 100")
    private int quantity;

    private String imageUrl;
}
