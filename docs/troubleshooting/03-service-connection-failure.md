# Troubleshooting Lab 3: Service Connection Failure

## Symptom

Order service cannot reach inventory service:
```
feign.RetryableException: Connection refused executing GET http://inventory-service:8085/api/inventory/PROD-001
```

## Diagnosis Steps

```bash
# 1. Verify inventory-service pod is running
kubectl get pods -n shopsphere-dev -l app=inventory-service

# 2. Verify the Service exists and has correct selectors
kubectl get svc inventory-service -n shopsphere-dev -o yaml
kubectl get endpoints inventory-service -n shopsphere-dev

# 3. Test DNS resolution from within the cluster
kubectl run debug-pod --image=busybox:1.36 --restart=Never -n shopsphere-dev \
  -- sh -c "nslookup inventory-service && wget -qO- http://inventory-service:8085/actuator/health"
kubectl logs debug-pod -n shopsphere-dev
kubectl delete pod debug-pod -n shopsphere-dev

# 4. Check NetworkPolicy
kubectl get networkpolicy -n shopsphere-dev
kubectl describe networkpolicy allow-inventory-service -n shopsphere-dev
```

## Common Causes & Fixes

### Cause 1: Endpoints list is empty (selector mismatch)
```bash
kubectl get endpoints inventory-service -n shopsphere-dev
# NAME                 ENDPOINTS   AGE
# inventory-service    <none>      5m  ← problem!

# Check pod labels
kubectl get pods -n shopsphere-dev -l app=inventory-service --show-labels

# Ensure Service selector matches pod labels
kubectl describe svc inventory-service -n shopsphere-dev | grep Selector
```

### Cause 2: NetworkPolicy blocking inter-service traffic
```bash
# Temporarily disable NetworkPolicy to test
kubectl delete networkpolicy allow-inventory-service -n shopsphere-dev

# If service works after deletion, the NetworkPolicy was misconfigured
# Fix: Ensure allow-internal policy allows pod-to-pod traffic
```

### Cause 3: Service port mismatch
```bash
# Check what port the container actually listens on
kubectl exec -it deployment/inventory-service -n shopsphere-dev -- \
  sh -c "ss -tlnp | grep LISTEN"

# Compare with Service targetPort
kubectl get svc inventory-service -n shopsphere-dev \
  -o jsonpath='{.spec.ports[0].targetPort}'
```

### Cause 4: inventory-service not yet started (readinessProbe failing)
```bash
kubectl describe endpoints inventory-service -n shopsphere-dev
# If NotReadyAddresses is populated, the pod is running but not ready

kubectl describe pod -n shopsphere-dev -l app=inventory-service | grep -A10 Conditions
```

## Reproduce the Lab

```bash
# Scale down inventory-service to 0 replicas
kubectl scale deployment inventory-service --replicas=0 -n shopsphere-dev

# Try to place an order (should fail with connection refused)
TOKEN=$(curl -s -X POST http://shopsphere.local/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

curl -X POST http://shopsphere.local/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":"PROD-001","quantity":1}]}'
# Expect 503 or 500 error

# Restore
kubectl scale deployment inventory-service --replicas=2 -n shopsphere-dev
```
