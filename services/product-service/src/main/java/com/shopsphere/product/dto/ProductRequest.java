package com.shopsphere.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private boolean active = true;
}
