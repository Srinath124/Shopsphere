# Service Level Objectives (SLOs) & Service Level Indicators (SLIs)
## ShopSphere / Go N Buy Platform

This document defines the production reliability targets, metrics collection formulas, and multi-window burn rate alert thresholds across the ShopSphere microservices platform.

---

## 1. Executive Summary Table

| Service / User Journey | Metric | SLI Specification | Target SLO (30-day window) | Error Budget |
| :--- | :--- | :--- | :--- | :--- |
| **Platform Availability** | Overall HTTP Success | `sum(2xx, 3xx, 4xx) / sum(all requests)` | **99.9%** (Three Nines) | **0.1%** (~43.8 minutes downtime/month) |
| **Catalog Browsing** | Latency | `p95(GET /api/products) < 300ms` | **99.0%** of requests | **1.0%** exceeding 300ms |
| **Order Placement** | Latency | `p95(POST /api/orders) < 800ms` | **99.0%** of requests | **1.0%** exceeding 800ms |
| **Inventory Reservation**| Correctness | Zero oversold inventory under race conditions | **100.0%** | **0.0%** (Zero Tolerance) |
| **Notification Dispatch**| Delivery Lag | Event consumed & processed `< 5000ms` | **99.5%** of events | **0.5%** |

---

## 2. Formal SLI Prometheus Formulas

### 2.1 API Availability SLI
```promql
# Ratio of successful HTTP responses over total HTTP responses across all services
sum(rate(http_server_requests_seconds_count{status!~"5.."}[30d])) 
/ 
sum(rate(http_server_requests_seconds_count[30d]))
```

### 2.2 Catalog Browsing Latency SLI (p95 < 300ms)
```promql
# Fraction of requests completing in under 300ms
sum(rate(http_server_requests_seconds_bucket{service="product-service", le="0.3"}[30d])) 
/ 
sum(rate(http_server_requests_seconds_count{service="product-service"}[30d]))
```

### 2.3 Checkout Latency SLI (p95 < 800ms)
```promql
sum(rate(http_server_requests_seconds_bucket{service="order-service", le="0.8"}[30d])) 
/ 
sum(rate(http_server_requests_seconds_count{service="order-service"}[30d]))
```

---

## 3. Multi-Window Multi-Burn-Rate Alerting

To prevent alert fatigue and catch fast outages without noisy false alarms, we employ Google SRE multi-window burn rate alerting:

| Burn Rate | % Budget Consumed | Short Window | Long Window | Severity | Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **14.4x** | 2% consumed in 1 hour | 5 minutes | 1 hour | **Page (P1)** | Immediate on-call escalation |
| **6x** | 5% consumed in 6 hours | 30 minutes | 6 hours | **Page (P2)** | Incident commander triage |
| **1x** | 10% consumed in 3 days | 2 hours | 3 days | **Ticket** | Daily standup prioritization |
