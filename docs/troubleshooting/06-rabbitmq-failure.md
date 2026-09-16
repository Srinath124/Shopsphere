# Troubleshooting Lab 6: RabbitMQ Failure

## Symptom

Orders are placed but notifications are never sent. Order service logs show:
```
WARN  c.s.order.messaging.OrderEventPublisher - Failed to publish order event, retrying...
com.rabbitmq.client.AlreadyClosedException: channel is already closed due to connection error
```

## Diagnosis Steps

```bash
# 1. Check RabbitMQ container
docker compose ps rabbitmq
docker compose logs rabbitmq --tail=30

# 2. Access RabbitMQ Management UI
# Open: http://localhost:15672  (guest/guest in dev)
# Check: Queues tab → shopsphere.orders queue

# 3. Check queue status from CLI
docker compose exec rabbitmq rabbitmqctl list_queues name messages consumers

# 4. Check dead-letter queue
docker compose exec rabbitmq rabbitmqctl list_queues | grep dead-letter

# 5. Check order-service publisher health
docker compose exec order-service curl -s http://localhost:8084/actuator/health | \
  python3 -m json.tool | grep -A5 rabbit
```

## Common Causes & Fixes

### Cause 1: RabbitMQ not started
```bash
docker compose up -d rabbitmq
# Wait for broker to be ready
until docker compose exec rabbitmq rabbitmqctl status 2>/dev/null | grep -q "RabbitMQ"; do
  echo "Waiting for RabbitMQ..."; sleep 2
done
```

### Cause 2: Queue/exchange not declared (first boot race)
```bash
# Check if exchanges exist
docker compose exec rabbitmq rabbitmqctl list_exchanges | grep shopsphere

# If missing, restart order-service (it declares them on startup)
docker compose restart order-service notification-service
```

### Cause 3: Dead-letter queue accumulating — messages poisoned
```bash
# Count messages in DLQ
docker compose exec rabbitmq rabbitmqctl list_queues name messages | grep dead

# Inspect DLQ messages from Management UI: Queues → shopsphere.orders.dlq → Get Messages
# If messages are malformed, clear the DLQ after investigation
docker compose exec rabbitmq rabbitmqctl purge_queue shopsphere.orders.dlq
```

### Cause 4: notification-service not consuming (no active consumers)
```bash
# Check consumers on queue
docker compose exec rabbitmq rabbitmqctl list_queues name consumers | grep orders

# If consumers=0, restart notification-service
docker compose restart notification-service
```

## Reproduce the Lab

```bash
# 1. Stop RabbitMQ
docker compose stop rabbitmq

# 2. Place an order
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"testuser","password":"password123"}' | jq -r .token)

curl -X POST http://localhost:8084/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"idempotencyKey":"test-key-lab6","items":[{"productId":"PROD-001","quantity":1}]}'

# Order is created but event publish fails — check logs
docker compose logs order-service | grep -i "rabbitmq\|event\|publish"

# 3. Restore RabbitMQ
docker compose start rabbitmq
sleep 10

# 4. Check if notification-service processes the event once RabbitMQ restores
docker compose logs notification-service | grep -i "received\|order"
```
