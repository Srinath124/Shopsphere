# System Architecture & Component Interaction
## ShopSphere / Go N Buy Platform

### 1. High-Level Architectural Diagram

```mermaid
graph TD
    User([End User / Browser]) -->|HTTP / HTTPS| Ingress[NGINX Reverse Proxy / Ingress Controller]

    subgraph Presentation & Gateway Tier
        Ingress -->|/| Frontend[React + Vite Frontend SPA]
        Ingress -->|/api/auth| AuthSvc[auth-service :8081]
        Ingress -->|/api/products| ProductSvc[product-service :8082]
        Ingress -->|/api/cart| CartSvc[cart-service :8083]
        Ingress -->|/api/orders| OrderSvc[order-service :8084]
        Ingress -->|/api/inventory| InvSvc[inventory-service :8085]
        Ingress -->|/api/notifications| NotifSvc[notification-service :8086]
    end

    subgraph Data & Caching Tier
        AuthSvc --> AuthDB[(auth_db PostgreSQL)]
        ProductSvc --> ProductDB[(product_db PostgreSQL)]
        OrderSvc --> OrderDB[(order_db PostgreSQL)]
        InvSvc --> InvDB[(inventory_db PostgreSQL)]
        NotifSvc --> NotifDB[(notification_db PostgreSQL)]
        CartSvc --> RedisCache[(Redis Cart Store)]
    end

    subgraph Asynchronous Event Mesh
        OrderSvc -->|Publish orders.exchange| RabbitMQ{{RabbitMQ Topic Broker}}
        RabbitMQ -->|notifications.queue| NotifSvc
        RabbitMQ -->|inventory.events.queue| InvSvc
    end

    subgraph Synchronous Microservice Calls
        OrderSvc -->|REST WebClient Check & Reserve| InvSvc
        CartSvc -->|REST Product Validation| ProductSvc
    end
```

---

### 2. Order Lifecycle & Event Orchestration State Machine

```mermaid
stateDiagram-v2
    [*] --> OrderCreated: User submits checkout (POST /api/orders with Idempotency-Key)
    OrderCreated --> InventoryReserved: WebClient synchronous reservation with SELECT FOR UPDATE
    InventoryReserved --> PaymentProcessed: Payment execution (Simulated gateway)
    
    PaymentProcessed --> Confirmed: Payment Success
    Confirmed --> PublishOrderConfirmed: Publish to RabbitMQ (order.confirmed)
    PublishOrderConfirmed --> DispatchNotification: notification-service consumes event
    DispatchNotification --> [*]

    InventoryReserved --> Cancelled: Payment Failed
    Cancelled --> RollbackInventory: Release locked stock back to available pool
    RollbackInventory --> PublishOrderCancelled: Publish to RabbitMQ (order.cancelled)
    PublishOrderCancelled --> [*]
```

---

### 3. Observability Architecture

```mermaid
graph LR
    subgraph Microservice Pods
        S1[auth-service]
        S2[product-service]
        S3[order-service]
        S4[inventory-service]
    end

    subgraph Metrics & Logging Pipeline
        Prometheus[Prometheus Server :9090] -->|Scrapes /actuator/prometheus| S1 & S2 & S3 & S4
        Promtail[Promtail Agent] -->|Reads Container Logs| S1 & S2 & S3 & S4
        Promtail --> Loki[Loki Log Aggregator :3100]
    end

    subgraph Visualization & Alerting
        Grafana[Grafana Dashboard :3001] -->|PromQL Queries| Prometheus
        Grafana -->|LogQL Queries| Loki
        Prometheus -->|Evaluates SLO rules| AlertManager[Alerting Engine]
    end
```
