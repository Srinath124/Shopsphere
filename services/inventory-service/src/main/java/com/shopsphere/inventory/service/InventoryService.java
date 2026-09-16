package com.shopsphere.inventory.service;

import com.shopsphere.inventory.domain.InventoryItem;
import com.shopsphere.inventory.dto.InventoryResponse;
import com.shopsphere.inventory.dto.StockRequest;
import com.shopsphere.inventory.exception.InsufficientStockException;
import com.shopsphere.inventory.exception.ResourceNotFoundException;
import com.shopsphere.inventory.repository.InventoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Inventory service with concurrency-safe stock management.
 *
 * Uses pessimistic locking (SELECT ... FOR UPDATE) to prevent race conditions.
 *
 * Example: Stock = 10
 *   Request A: reserve 8 → succeeds (stock now: total=10, reserved=8, available=2)
 *   Request B: reserve 5 → fails (available=2 < requested=5)
 *
 * The pessimistic lock ensures Request B waits for Request A to commit
 * before reading stock levels, preventing overselling.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final Counter reservationFailureCounter;
    private final Counter reservationSuccessCounter;

    public InventoryService(InventoryRepository inventoryRepository, MeterRegistry meterRegistry) {
        this.inventoryRepository = inventoryRepository;
        this.reservationFailureCounter = Counter.builder("shopsphere.inventory.reservation.failures")
                .description("Number of failed inventory reservations (insufficient stock)")
                .register(meterRegistry);
        this.reservationSuccessCounter = Counter.builder("shopsphere.inventory.reservation.success")
                .description("Number of successful inventory reservations")
                .register(meterRegistry);
    }

    public InventoryResponse getInventory(UUID productId) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        return mapToResponse(item);
    }

    @Transactional
    public InventoryResponse createOrUpdateInventory(UUID productId, int totalQuantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .map(existing -> {
                    existing.setTotalQuantity(totalQuantity);
                    return existing;
                })
                .orElse(InventoryItem.builder()
                        .productId(productId)
                        .totalQuantity(totalQuantity)
                        .reservedQuantity(0)
                        .build());

        InventoryItem saved = inventoryRepository.save(item);
        log.info("Inventory set for product {}: total={}", productId, totalQuantity);
        return mapToResponse(saved);
    }

    /**
     * Reserve stock for a product.
     * Uses pessimistic lock to prevent concurrent over-reservation.
     */
    @Transactional
    public InventoryResponse reserveStock(UUID productId, StockRequest request) {
        // Pessimistic lock prevents concurrent reads until transaction commits
        InventoryItem item = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));

        try {
            item.reserve(request.getQuantity());
            InventoryItem saved = inventoryRepository.save(item);
            reservationSuccessCounter.increment();
            log.info("Reserved {} units for product {}. Available: {}", 
                request.getQuantity(), productId, saved.getAvailableQuantity());
            return mapToResponse(saved);
        } catch (IllegalStateException e) {
            reservationFailureCounter.increment();
            log.warn("Reserve failed for product {}: {}", productId, e.getMessage());
            throw new InsufficientStockException(e.getMessage());
        }
    }

    /**
     * Release previously reserved stock (e.g., order cancelled).
     */
    @Transactional
    public InventoryResponse releaseStock(UUID productId, StockRequest request) {
        InventoryItem item = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));

        item.release(request.getQuantity());
        InventoryItem saved = inventoryRepository.save(item);
        log.info("Released {} units for product {}. Available: {}", 
            request.getQuantity(), productId, saved.getAvailableQuantity());
        return mapToResponse(saved);
    }

    /**
     * Deduct stock after successful order completion (reserved → deducted).
     */
    @Transactional
    public InventoryResponse deductStock(UUID productId, StockRequest request) {
        InventoryItem item = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));

        try {
            item.deduct(request.getQuantity());
            InventoryItem saved = inventoryRepository.save(item);
            log.info("Deducted {} units for product {}. Total remaining: {}", 
                request.getQuantity(), productId, saved.getTotalQuantity());
            return mapToResponse(saved);
        } catch (IllegalStateException e) {
            log.warn("Deduct failed for product {}: {}", productId, e.getMessage());
            throw new InsufficientStockException(e.getMessage());
        }
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .totalQuantity(item.getTotalQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getAvailableQuantity())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
