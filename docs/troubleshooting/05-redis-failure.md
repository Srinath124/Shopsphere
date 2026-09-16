# Troubleshooting Lab 5: Redis Failure

## Symptom

Cart service returns 500 errors or cart operations fail:
```
org.springframework.data.redis.RedisConnectionFailureException:
  Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException:
  Unable to connect to redis:6379
```

## Diagnosis Steps

```bash
# 1. Check Redis container status (Docker Compose)
docker compose ps redis
docker compose logs redis --tail=20

# 2. Ping Redis
docker compose exec redis redis-cli ping
# Expected: PONG

# 3. Check Redis memory usage (OOM)
docker compose exec redis redis-cli info memory | grep used_memory_human

# 4. Check cart-service Redis configuration
kubectl exec -it deployment/cart-service -n shopsphere-dev -- \
  printenv | grep -E 'REDIS'

# 5. From within a pod, test Redis reachability
kubectl exec -it deployment/cart-service -n shopsphere-dev -- \
  sh -c "nc -zv \$SPRING_DATA_REDIS_HOST \$SPRING_DATA_REDIS_PORT && echo OK"
```

## Common Causes & Fixes

### Cause 1: Redis container not running
```bash
docker compose up -d redis
# Wait for Redis to be ready
until docker compose exec redis redis-cli ping 2>/dev/null | grep -q PONG; do
  echo "Waiting for Redis..."; sleep 1
done
```

### Cause 2: Redis maxmemory policy evicting all keys
```bash
# Check Redis config
docker compose exec redis redis-cli config get maxmemory
docker compose exec redis redis-cli config get maxmemory-policy

# For cart data, use allkeys-lru so least recently used carts are evicted
docker compose exec redis redis-cli config set maxmemory-policy allkeys-lru
```

### Cause 3: Redis AUTH password mismatch
```bash
# If Redis is configured with requirepass, ensure cart-service
# has SPRING_DATA_REDIS_PASSWORD set correctly
docker compose exec redis redis-cli -a wrongpass ping
# Should fail with WRONGPASS

# Fix: Update the password in .env and restart cart-service
```

### Cause 4: Network partition (Kubernetes)
```bash
# Check NetworkPolicy allows cart-service to reach Redis
kubectl get networkpolicy -n shopsphere-dev
# The default-deny policy blocks all egress not explicitly allowed
# Redis runs outside k8s in dev (Docker Compose), so ensure correct host

kubectl describe configmap shopsphere-config -n shopsphere-dev | grep REDIS
```

## Reproduce the Lab

```bash
# 1. Stop Redis
docker compose stop redis

# 2. Try adding an item to cart
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"testuser","password":"password123"}' | jq -r .token)

curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-001","quantity":2}'
# Expect 500 error

# 3. Restore Redis
docker compose start redis
sleep 5

# 4. Retry cart operation — should succeed
curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-001","quantity":2}'
# Expect 200 OK
```
