package com.shopsphere.inventory.controller;

import com.shopsphere.inventory.dto.InventoryResponse;
import com.shopsphere.inventory.dto.StockRequest;
import com.shopsphere.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory status for a product")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping("/{productId}/reserve")
    @Operation(summary = "Reserve stock for a product (used by order-service)")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(productId, request));
    }

    @PostMapping("/{productId}/release")
    @Operation(summary = "Release reserved stock (used when order is cancelled)")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(inventoryService.releaseStock(productId, request));
    }

    @PostMapping("/{productId}/deduct")
    @Operation(summary = "Deduct reserved stock after successful order")
    public ResponseEntity<InventoryResponse> deductStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(inventoryService.deductStock(productId, request));
    }

    @PostMapping("/{productId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set total stock for a product (admin only)")
    public ResponseEntity<InventoryResponse> setStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(inventoryService.createOrUpdateInventory(productId, request.getQuantity()));
    }
}
