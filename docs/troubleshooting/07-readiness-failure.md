# Troubleshooting Lab 7: Readiness Probe Failure

## Symptom

Pod is Running but never becomes Ready, no traffic is routed to it:
```
kubectl get pods -n shopsphere-dev
NAME                          READY   STATUS    RESTARTS   AGE
order-service-7f9d-xxxxx      0/1     Running   0          5m
```

## Diagnosis Steps

```bash
# 1. Describe pod — look at Conditions and Events
kubectl describe pod -n shopsphere-dev -l app=order-service | \
  grep -A20 "Conditions:\|Events:"

# 2. Check readiness probe configuration
kubectl get pod -n shopsphere-dev -l app=order-service \
  -o jsonpath='{.items[0].spec.containers[0].readinessProbe}' | python3 -m json.tool

# 3. Manually test the readiness endpoint from within the pod
kubectl exec -it deployment/order-service -n shopsphere-dev -- \
  wget -qO- http://localhost:8084/actuator/health/readiness

# 4. Check application logs for what's failing
kubectl logs -n shopsphere-dev -l app=order-service | tail -50

# 5. Check endpoints (empty = not ready)
kubectl get endpoints order-service -n shopsphere-dev
```

## Common Causes & Fixes

### Cause 1: Dependency not ready (database, RabbitMQ, Redis)
Spring Boot's readiness probe checks all health indicators including DB and messaging.
```
{"status":"OUT_OF_SERVICE","components":{"db":{"status":"DOWN"}}}
```
**Fix:** Wait for all dependencies to be healthy, or use startupProbe to give more time.

### Cause 2: readinessProbe path wrong
```bash
# Test the path manually
kubectl exec -it deployment/order-service -n shopsphere-dev -- \
  wget -qO- http://localhost:8084/actuator/health/readiness
# If 404, wrong path. Ensure management.health.probes.enabled=true in application.yml
```

### Cause 3: Port mismatch in probe
```bash
# Check what port the app is listening on
kubectl exec -it deployment/order-service -n shopsphere-dev -- \
  sh -c "cat /proc/*/net/tcp6 2>/dev/null | grep ' 00000000:20D4 ' | head -3"
# 0x20D4 = 8404 decimal? Recalculate: expected 8084 = 0x1F94

# Fix: correct containerPort and readinessProbe port in deployment
```

### Cause 4: application.yml missing actuator config
```yaml
# Ensure this is in application.yml:
management:
  health:
    probes:
      enabled: true
  endpoint:
    health:
      show-details: always
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
```

## Reproduce the Lab

```bash
# Temporarily configure wrong readiness path
kubectl patch deployment order-service -n shopsphere-dev \
  --type='json' -p='[{
    "op":"replace",
    "path":"/spec/template/spec/containers/0/readinessProbe/httpGet/path",
    "value":"/actuator/nonexistent"
  }]'

# Watch pod fail to become Ready
kubectl get pods -n shopsphere-dev -w

# Verify traffic stops reaching the service
kubectl get endpoints order-service -n shopsphere-dev

# Restore correct path
kubectl patch deployment order-service -n shopsphere-dev \
  --type='json' -p='[{
    "op":"replace",
    "path":"/spec/template/spec/containers/0/readinessProbe/httpGet/path",
    "value":"/actuator/health/readiness"
  }]'
```
