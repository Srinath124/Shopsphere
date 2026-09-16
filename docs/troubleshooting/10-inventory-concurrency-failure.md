# Troubleshooting Lab 10: Inventory Concurrency Failure

## Symptom

Multiple concurrent orders result in overselling (stock goes negative):
```
Order PLACED for 5 units of PROD-001
Order PLACED for 5 units of PROD-001
→ Stock: -2 (should have been blocked at 0)
```

Or alternatively, excessive lock timeouts during flash sales:
```
org.springframework.dao.CannotAcquireLockException:
  could not obtain lock on row in relation "inventory_items"
```

## Background

The inventory-service uses **pessimistic locking** (`SELECT ... FOR UPDATE`) to prevent
concurrent overselling. This lab demonstrates what happens when locking is bypassed or
incorrectly implemented, and how to verify correct behavior.

## Diagnosis Steps

```bash
# 1. Check current stock level
curl -s http://localhost:8085/api/inventory/PROD-001 | python3 -m json.tool

# 2. Check if pessimistic lock is working (view active locks in Postgres)
docker compose exec postgres psql -U shopsphere -d inventory_db -c \
  "SELECT pid, state, wait_event_type, wait_event, query
   FROM pg_stat_activity
   WHERE query LIKE '%FOR UPDATE%';"

# 3. Check for deadlocks in Postgres logs
docker compose exec postgres psql -U shopsphere -d inventory_db -c \
  "SELECT * FROM pg_stat_activity WHERE wait_event = 'relation';"

# 4. Run the concurrency test
k6 run tests/load/k6-concurrency-lock-test.js \
  --vus 50 --duration 30s

# 5. Verify stock never went negative
curl -s http://localhost:8085/api/inventory/PROD-001 | jq .quantity
```

## Common Causes & Fixes

### Cause 1: Missing `@Lock(LockModeType.PESSIMISTIC_WRITE)` annotation
```java
// WRONG: No lock, allows concurrent reads → overselling
@Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
Optional<InventoryItem> findByProductId(@Param("productId") String productId);

// CORRECT: Pessimistic lock
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
Optional<InventoryItem> findByProductIdWithLock(@Param("productId") String productId);
```

### Cause 2: `@Transactional` missing on service method
```java
// The @Transactional is required for the pessimistic lock to be held
// across the read-modify-write cycle
@Transactional  // ← MUST be present
public void reserveStock(String productId, int quantity) {
    InventoryItem item = repo.findByProductIdWithLock(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
    if (item.getQuantity() < quantity) {
        throw new InsufficientStockException(productId, quantity, item.getQuantity());
    }
    item.setQuantity(item.getQuantity() - quantity);
    repo.save(item);
}
```

### Cause 3: Lock timeout too short
```yaml
# application.yml
spring:
  jpa:
    properties:
      jakarta:
        persistence:
          lock:
            timeout: 5000  # 5 seconds; increase if necessary
```

### Cause 4: Multiple replicas without shared lock mechanism
Each replica acquires its own DB lock, which is correct. But if a load balancer
sends two requests for the same product to different replicas simultaneously,
they will serialize on the DB row lock correctly — this is the expected behavior.

## Reproduce the Lab

```bash
# 1. Set inventory to exactly 5 units
curl -X PUT http://localhost:8085/api/inventory/PROD-001 \
  -H 'Content-Type: application/json' \
  -d '{"quantity": 5}'

# 2. Send 10 concurrent requests each requesting 1 unit
for i in {1..10}; do
  curl -s -X POST http://localhost:8084/api/orders \
    -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' \
    -d "{\"idempotencyKey\":\"lab10-test-$i\",\"items\":[{\"productId\":\"PROD-001\",\"quantity\":1}]}" \
    -o /dev/null &
done
wait

# 3. Check final stock — should be 0, never negative
curl -s http://localhost:8085/api/inventory/PROD-001 | jq .quantity
# Expected: 0

# 4. Count successful vs failed orders
echo "Expected: 5 PLACED orders, 5 INSUFFICIENT_STOCK errors"

# 5. Run automated k6 test for reproducibility
k6 run tests/load/k6-concurrency-lock-test.js --vus 50 --duration 15s
# Check: checks{concurrent_orders_prevent_oversell:true} = 100%
```
