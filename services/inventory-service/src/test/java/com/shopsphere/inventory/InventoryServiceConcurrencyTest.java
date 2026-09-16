package com.shopsphere.inventory;

import com.shopsphere.inventory.domain.InventoryItem;
import com.shopsphere.inventory.dto.StockRequest;
import com.shopsphere.inventory.exception.InsufficientStockException;
import com.shopsphere.inventory.repository.InventoryRepository;
import com.shopsphere.inventory.service.InventoryService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@DisplayName("Inventory Service Concurrency Tests")
class InventoryServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventory_db")
            .withUsername("shopsphere")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    private UUID productId;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        productId = UUID.randomUUID();
        inventoryService.createOrUpdateInventory(productId, 10);
    }

    @Test
    @DisplayName("Should prevent overselling - concurrent reservations should not exceed total stock")
    void shouldPreventOverselling() throws InterruptedException {
        int totalThreads = 10;
        int quantityPerThread = 2; // Each thread tries to reserve 2 units; total=20 > stock=10

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // All threads start simultaneously
                    StockRequest request = new StockRequest();
                    request.setQuantity(quantityPerThread);
                    inventoryService.reserveStock(productId, request);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown(); // Release all threads at once
        doneLatch.await();
        executor.shutdown();

        // Only 5 threads should succeed (5 * 2 = 10 = total stock)
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failureCount.get()).isEqualTo(5);

        // Verify final state
        var inventory = inventoryService.getInventory(productId);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Request B must fail when stock is already reserved by Request A")
    void requestBShouldFailWhenInsufficientStock() {
        // Request A reserves 8 (leaves 2 available)
        StockRequest requestA = new StockRequest();
        requestA.setQuantity(8);
        inventoryService.reserveStock(productId, requestA);

        // Request B attempts to reserve 5 - must fail
        StockRequest requestB = new StockRequest();
        requestB.setQuantity(5);

        assertThatThrownBy(() -> inventoryService.reserveStock(productId, requestB))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        // Verify state unchanged
        var inventory = inventoryService.getInventory(productId);
        assertThat(inventory.getReservedQuantity()).isEqualTo(8);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Release should restore available quantity")
    void shouldRestoreStockOnRelease() {
        StockRequest reserveRequest = new StockRequest();
        reserveRequest.setQuantity(8);
        inventoryService.reserveStock(productId, reserveRequest);

        StockRequest releaseRequest = new StockRequest();
        releaseRequest.setQuantity(8);
        inventoryService.releaseStock(productId, releaseRequest);

        var inventory = inventoryService.getInventory(productId);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(10);
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);
    }
}
