# Data Flow & Concurrency Controls
## ShopSphere / Go N Buy Platform

### 1. High-Concurrency Pessimistic Inventory Locking

In high-volume e-commerce flash sales, optimistic locking often leads to repeated rollback retries and poor user experience under hot-key contention. ShopSphere implements explicit database-level row locking:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
Optional<Inventory> findByProductIdWithLock(@Param("productId") String productId);
```

#### Lock Lifecycle:
1. `order-service` requests stock reservation via REST endpoint: `POST /api/inventory/reserve`.
2. `inventory-service` begins an `@Transactional` boundary.
3. PostgreSQL executes `SELECT * FROM inventory WHERE product_id = ? FOR UPDATE`.
4. Concurrent incoming reservation requests for the same SKU block at the database engine until the active transaction commits or rolls back.
5. If `available_stock >= requested_quantity`, `available_stock` is decremented and `reserved_stock` is incremented.
6. If insufficient, an immediate `InsufficientStockException` returns HTTP 409 Conflict.

---

### 2. Idempotent Order Creation

To prevent duplicate charges caused by network drops, mobile retries, or user double-clicks:

1. The client generates a unique UUID `Idempotency-Key` in the request headers:
   ```http
   POST /api/orders HTTP/1.1
   Idempotency-Key: e8c44f5c-8df2-4217-9154-1b0387b35582
   ```
2. The `orders` database schema enforces uniqueness:
   ```sql
   CREATE UNIQUE INDEX idx_orders_idempotency_key 
   ON orders(idempotency_key) 
   WHERE idempotency_key IS NOT NULL;
   ```
3. `order-service` first checks `orderRepository.findByIdempotencyKey(key)`. If found, it immediately returns the existing `OrderResponse` without charging again or creating new reservations.

---

### 3. Distributed Event Mesh via RabbitMQ

Order state transitions publish structured JSON events onto the `orders.exchange` topic exchange:

| Routing Key | Event Type | Consumer | Action Taken |
| :--- | :--- | :--- | :--- |
| `order.created` | `ORDER_CREATED` | `notification-service` | Sends initial email receipt |
| `order.confirmed` | `ORDER_CONFIRMED` | `notification-service` | Sends order confirmation receipt |
| `order.cancelled` | `ORDER_CANCELLED` | `inventory-service` | Releases reserved inventory back to available stock |
| `order.cancelled` | `ORDER_CANCELLED` | `notification-service` | Dispatches cancellation notice |
| `dead-letter` | Dead-letter payload | `notifications.dlq` | Stored for operator inspection and retry |
