package com.shopsphere.inventory.repository;

import com.shopsphere.inventory.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findByProductId(UUID productId);

    /**
     * Pessimistic write lock for critical stock operations.
     * Prevents concurrent reads during reserve/deduct operations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
    Optional<InventoryItem> findByProductIdForUpdate(@Param("productId") UUID productId);
}
