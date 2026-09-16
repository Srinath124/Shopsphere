package com.shopsphere.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private String userId;
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal totalAmount;
}
