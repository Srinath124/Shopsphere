# Chaos Engineering Experiment Runbook
## ShopSphere / Go N Buy Platform

This document specifies chaos engineering experiments designed to prove platform resilience against network latency, pod failures, and broker disconnections.

---

### Experiment 1: Inventory Service Pod Termination during Active Checkout
- **Hypothesis**: Terminating an `inventory-service` pod while orders are placing will cause temporary request retries at WebClient without losing order consistency or dropping HTTP 500s to the user.
- **Chaos Tool**: Chaos Mesh / LitmusChaos
- **Execution**:
  ```bash
  kubectl delete pod -n shopsphere-prod -l app=inventory-service --now
  ```
- **Verification**: Run k6 checkout load test during termination. Assert `http_req_failed < 0.01`.

---

### Experiment 2: RabbitMQ Network Partition Injection
- **Hypothesis**: Introducing 500ms network latency or dropping connections to RabbitMQ will not block the synchronous checkout API; order-service will buffer/retry or return confirmed order, with notifications processing as soon as connection heals.
- **Verification**: Inspect Prometheus queue depth `rabbitmq_queue_messages_unacknowledged` and dead-letter queue metrics.

---

### Experiment 3: Redis Cache Eviction & Recovery
- **Hypothesis**: Flushing Redis while user carts are active will not crash `cart-service`.
- **Verification**:
  ```bash
  redis-cli flushall
  ```
  Users receive an empty cart without 500 internal server errors.
