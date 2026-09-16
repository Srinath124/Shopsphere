package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * Supports idempotency via the Idempotency-Key header.
     * If the same key is submitted twice, the second request returns the existing order.
     */
    @PostMapping
    @Operation(summary = "Create a new order (idempotent via Idempotency-Key header)")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        OrderResponse response = orderService.createOrder(userDetails.getUsername(), request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders for current user")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userDetails.getUsername()));
    }
}
