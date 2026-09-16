# Incident Response Runbook & Severity Matrix
## ShopSphere / Go N Buy Platform

### 1. Incident Severity Levels

| Severity | Definition | Target MTTA (Ack) | Target MTTR (Mitigate) | Example |
| :--- | :--- | :--- | :--- | :--- |
| **P1 — Critical** | Total platform outage or core checkout flow broken. Multiple services unreachable. | `< 5 mins` | `< 30 mins` | Order placement returning HTTP 500 across all users; DB failover failure. |
| **P2 — Major** | Degraded functionality or single non-critical microservice down. High error rate on browsing. | `< 15 mins` | `< 2 hours` | Notification service failing to send emails; Cart cache latency > 2s. |
| **P3 — Minor** | Low impact issue with available workaround. Single pod crash loop in multi-replica pool. | `< 1 hour` | `< 1 business day` | Inaccurate badge counts; localized UI rendering defect. |

---

### 2. Incident Command Roles
- **Incident Commander (IC)**: Leads triage, delegates tasks, directs rollbacks, makes final decisions.
- **Operations Lead (Ops)**: Executes debugging commands, inspects logs, applies configuration changes.
- **Communications Lead (Comms)**: Updates internal stakeholders and customer status pages every 15 minutes.

---

### 3. Immediate Diagnostic Runbook

#### Step 1: Check Pod Health
```bash
kubectl get pods -n shopsphere-prod -o wide
kubectl top pods -n shopsphere-prod
```

#### Step 2: Stream Logs for Failing Services
```bash
kubectl logs -n shopsphere-prod -l app=order-service --tail=200 -f
```

#### Step 3: Check Database & Cache Connectivity
```bash
# Verify database connection pool metrics
curl -s http://localhost:8084/actuator/prometheus | grep hikaricp
# Verify Redis connectivity
redis-cli ping
```

#### Step 4: Rapid Rollback
If a recent deployment caused the regression:
```bash
helm rollback shopsphere -n shopsphere-prod
# OR via kubectl
kubectl rollout undo deployment/order-service -n shopsphere-prod
```
