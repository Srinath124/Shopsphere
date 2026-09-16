package com.shopsphere.cart.service;

import com.shopsphere.cart.dto.*;
import com.shopsphere.cart.model.Cart;
import com.shopsphere.cart.model.CartItem;
import com.shopsphere.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());
        return mapToResponse(cart);
    }

    public CartResponse addItem(String userId, AddItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());

        CartItem item = CartItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .unitPrice(request.getUnitPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        cart.addItem(request.getProductId(), item);
        cartRepository.save(cart);
        log.info("Added item {} to cart for user {}", request.getProductId(), userId);
        return mapToResponse(cart);
    }

    public CartResponse updateItem(String userId, String productId, UpdateItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found for user: " + userId));

        cart.updateItemQuantity(productId, request.getQuantity());
        cartRepository.save(cart);
        log.info("Updated item {} quantity to {} for user {}", productId, request.getQuantity(), userId);
        return mapToResponse(cart);
    }

    public CartResponse removeItem(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found for user: " + userId));

        cart.removeItem(productId);
        cartRepository.save(cart);
        log.info("Removed item {} from cart for user {}", productId, userId);
        return mapToResponse(cart);
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cleared cart for user {}", userId);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().values().stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .imageUrl(item.getImageUrl())
                        .build())
                .toList();

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}
