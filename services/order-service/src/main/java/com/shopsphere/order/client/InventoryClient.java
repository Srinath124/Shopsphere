package com.shopsphere.order.client;

import com.shopsphere.order.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for inventory-service.
 * Uses WebClient for non-blocking calls with timeout handling.
 *
 * IMPORTANT: Inventory reservation is NOT retried automatically because
 * retrying a reservation could double-reserve stock.
 * Order cancellation triggers release, which IS idempotent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.inventory-url}")
    private String inventoryServiceUrl;

    public void reserveStock(UUID productId, int quantity) {
        try {
            webClientBuilder.build()
                .post()
                .uri(inventoryServiceUrl + "/api/inventory/{productId}/reserve", productId)
                .bodyValue(Map.of("quantity", quantity))
                .retrieve()
                .onStatus(status -> status == HttpStatus.CONFLICT, response ->
                    Mono.error(new InventoryServiceException("Insufficient stock for product: " + productId)))
                .onStatus(HttpStatus::isError, response ->
                    Mono.error(new InventoryServiceException("Inventory service error for product: " + productId)))
                .bodyToMono(Void.class)
                .block();
            log.info("Reserved {} units of product {}", quantity, productId);
        } catch (InventoryServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Inventory service HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new InventoryServiceException("Failed to reserve stock: " + e.getMessage());
        } catch (Exception e) {
            log.error("Inventory service call failed: {}", e.getMessage());
            throw new InventoryServiceException("Inventory service unavailable: " + e.getMessage());
        }
    }

    public void releaseStock(UUID productId, int quantity) {
        try {
            webClientBuilder.build()
                .post()
                .uri(inventoryServiceUrl + "/api/inventory/{productId}/release", productId)
                .bodyValue(Map.of("quantity", quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            log.info("Released {} units of product {}", quantity, productId);
        } catch (Exception e) {
            log.error("Failed to release stock for product {}: {}", productId, e.getMessage());
            // Don't propagate release failures — order already cancelled
        }
    }

    public void deductStock(UUID productId, int quantity) {
        try {
            webClientBuilder.build()
                .post()
                .uri(inventoryServiceUrl + "/api/inventory/{productId}/deduct", productId)
                .bodyValue(Map.of("quantity", quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            log.info("Deducted {} units of product {}", quantity, productId);
        } catch (Exception e) {
            log.error("Failed to deduct stock for product {}: {}", productId, e.getMessage());
        }
    }
}
