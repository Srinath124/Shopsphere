package com.shopsphere.cart.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private String imageUrl;
}
