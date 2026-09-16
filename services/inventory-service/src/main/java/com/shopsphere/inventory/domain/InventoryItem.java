package com.shopsphere.inventory.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory item with concurrency-safe stock management.
 *
 * Uses optimistic locking (@Version) to prevent lost updates
 * when multiple requests modify stock simultaneously.
 *
 * Stock model:
 *   available_quantity = total_quantity - reserved_quantity
 *
 * A reservation succeeds only if available_quantity >= requested quantity.
 * This prevents overselling.
 */
@Entity
@Table(name = "inventory",
    indexes = {
        @Index(name = "idx_inventory_product_id", columnList = "product_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    /**
     * Optimistic locking version field.
     * If two transactions read the same version and both try to update,
     * the second will get an OptimisticLockingFailureException.
     * The application retries or reports an error.
     */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }

    public boolean hasAvailable(int quantity) {
        return getAvailableQuantity() >= quantity;
    }

    public void reserve(int quantity) {
        if (!hasAvailable(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    productId, getAvailableQuantity(), quantity));
        }
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        int newReserved = this.reservedQuantity - quantity;
        if (newReserved < 0) {
            newReserved = 0;
        }
        this.reservedQuantity = newReserved;
    }

    public void deduct(int quantity) {
        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException(
                "Cannot deduct more than reserved. Reserved: " + reservedQuantity + ", Requested: " + quantity);
        }
        this.reservedQuantity -= quantity;
        this.totalQuantity -= quantity;
    }
}
