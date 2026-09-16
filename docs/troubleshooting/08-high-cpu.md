# Troubleshooting Lab 8: High CPU

## Symptom

Prometheus alert fires: `HighCPUUsage — inventory-service CPU > 80%`
HPA has scaled the deployment to maxReplicas but CPU is still high.

## Diagnosis Steps

```bash
# 1. Check current CPU usage per pod
kubectl top pods -n shopsphere-dev -l app=inventory-service

# 2. Check HPA status
kubectl describe hpa inventory-service-hpa -n shopsphere-dev

# 3. Get thread dump from the running JVM
kubectl exec -it deployment/inventory-service -n shopsphere-dev -- \
  sh -c "kill -3 1 && sleep 2"
kubectl logs -n shopsphere-dev -l app=inventory-service --tail=200 | grep -A50 "Full thread dump"

# 4. Check Prometheus metrics
# Query: rate(process_cpu_usage[5m]) by (pod)
# Access: http://localhost:9090 (local) or via port-forward

# 5. Check for runaway loops or hot paths in application metrics
# Query: rate(http_server_requests_seconds_count{service="inventory-service"}[5m])
```

## Common Causes & Fixes

### Cause 1: Inventory concurrency thundering herd
During flash sales, hundreds of concurrent requests hit the pessimistic lock on inventory.
The database lock wait queue causes CPU to spike.

**Fix:** Apply rate-limiting at the API gateway/ingress level:
```yaml
# In ingress annotation (nginx):
nginx.ingress.kubernetes.io/limit-rps: "50"
nginx.ingress.kubernetes.io/limit-connections: "20"
```

### Cause 2: JVM GC thrashing (memory too low → frequent GC)
```bash
# Check GC metrics
kubectl exec -it deployment/inventory-service -n shopsphere-dev -- \
  sh -c "jstat -gcutil 1 1000 10"

# If OldGen is consistently > 90%, increase memory
# Fix: Increase memory limit in values.yaml or deployment
```

### Cause 3: N+1 query pattern causing DB CPU spike
```bash
# Check slow queries in Postgres
docker compose exec postgres psql -U shopsphere -d inventory_db -c \
  "SELECT query, calls, total_exec_time, mean_exec_time
   FROM pg_stat_statements
   ORDER BY total_exec_time DESC LIMIT 10;"

# Fix: Add @EntityGraph or JOIN FETCH in JPA repository
```

### Cause 4: k6 load test still running accidentally
```bash
# Check for running k6 processes
ps aux | grep k6

# Stop them
pkill -f k6

# Or check for open connections
ss -s
```

## Reproduce the Lab

```bash
# Run k6 high concurrency test against inventory
k6 run tests/load/k6-concurrency-lock-test.js \
  --vus 200 --duration 60s

# Observe CPU in another terminal
watch -n2 'kubectl top pods -n shopsphere-dev -l app=inventory-service'

# Observe HPA scaling
watch -n2 'kubectl get hpa -n shopsphere-dev'

# After test, verify CPU drops and HPA scales down
# (scale-down cooldown is 5 minutes by default)
```
