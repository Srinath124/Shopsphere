package com.shopsphere.cart.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Cart is stored in Redis as a serialized object.
 * Key pattern: cart:{userId}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart implements Serializable {

    private String userId;

    @Builder.Default
    private Map<String, CartItem> items = new HashMap<>();

    public void addItem(String productId, CartItem item) {
        if (items.containsKey(productId)) {
            CartItem existing = items.get(productId);
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            items.put(productId, item);
        }
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public void updateItemQuantity(String productId, int quantity) {
        if (!items.containsKey(productId)) {
            throw new IllegalArgumentException("Item not found in cart: " + productId);
        }
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            items.get(productId).setQuantity(quantity);
        }
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotalAmount() {
        return items.values().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
