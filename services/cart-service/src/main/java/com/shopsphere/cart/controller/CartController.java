package com.shopsphere.cart.controller;

import com.shopsphere.cart.dto.*;
import com.shopsphere.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userDetails.getUsername(), request));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userDetails.getUsername(), productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItem(userDetails.getUsername(), productId));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
