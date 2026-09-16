package com.shopsphere.inventory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private int totalQuantity;
    private int reservedQuantity;
    private int availableQuantity;
    private LocalDateTime updatedAt;
}
