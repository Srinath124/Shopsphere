package com.shopsphere.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private String imageUrl;
}
