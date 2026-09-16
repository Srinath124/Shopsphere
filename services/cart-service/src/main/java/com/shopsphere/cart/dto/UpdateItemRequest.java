package com.shopsphere.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateItemRequest {

    @Min(value = 0, message = "Quantity must be 0 or greater (0 removes the item)")
    @Max(value = 100, message = "Quantity must not exceed 100")
    private int quantity;
}
