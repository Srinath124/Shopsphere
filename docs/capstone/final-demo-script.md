# Capstone Final Live Demonstration Script
## ShopSphere / Go N Buy Platform

This step-by-step script guides presenters and evaluators through a complete 10-minute live demonstration of the platform.

---

### Step 1: Infrastructure & Service Launch (1 min)
1. Launch the platform stack:
   ```bash
   ./scripts/start.sh --with-monitoring
   ```
2. Verify all 6 backend services pass readiness probes:
   ```bash
   ./scripts/health-check.sh
   ```
   *Expected*: All 6 services report `HEALTHY (HTTP 200)`.

---

### Step 2: User Journey & Real-Time Event Choreography (3 mins)
1. Open the browser to `http://localhost`.
2. Register a new user (`testuser` / `password123`).
3. Browse products, filter by category, and observe live inventory availability counts.
4. Add 2 items to the cart. Observe the cart icon badge updating dynamically via Redis session storage.
5. Click **Proceed to Checkout**, enter address details, and click **Place Order**.
6. Navigate to **Notifications** tab: demonstrate the real-time event delivered from RabbitMQ (`ORDER_CREATED` & `ORDER_CONFIRMED`).

---

### Step 3: High-Concurrency Flash Sale & Race Condition Test (2 mins)
1. Trigger the k6 pessimistic locking test:
   ```bash
   k6 run tests/load/k6-concurrency-lock-test.js
   ```
2. Show that despite 50 concurrent buyers attempting to purchase simultaneously:
   - Zero deadlock errors occur.
   - Available stock drops cleanly without negative stock balances.
   - Overselling is 100% prevented by `PESSIMISTIC_WRITE` locks.

---

### Step 4: Observability & SRE Dashboards (2 mins)
1. Open Grafana: `http://localhost:3001` (admin / admin).
2. Open the **ShopSphere — Microservices Overview** dashboard.
3. Show:
   - Real-time RPS throughput across services.
   - p95 latency graphs.
   - JVM heap utilization.
   - HTTP 5xx error rate staying strictly under the 1% SLO boundary.

---

### Step 5: AIOps Telemetry Anomaly Detection (2 mins)
1. Run the Python AIOps script:
   ```bash
   python aiops/anomaly-detector.py
   ```
2. Explain the rolling Z-score evaluation and how sudden metric spikes trigger automated root-cause suggestions.
