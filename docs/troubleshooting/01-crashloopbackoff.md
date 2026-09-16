# Troubleshooting Lab 1: CrashLoopBackOff

## Symptom

```
kubectl get pods -n shopsphere-dev
NAME                           READY   STATUS             RESTARTS   AGE
auth-service-7d9f8b-xxxxx     0/1     CrashLoopBackOff   5          3m
```

## Diagnosis Steps

```bash
# 1. Check pod events
kubectl describe pod -n shopsphere-dev -l app=auth-service

# 2. Check container logs
kubectl logs -n shopsphere-dev -l app=auth-service --previous

# 3. Check resource limits
kubectl get pod -n shopsphere-dev -l app=auth-service -o jsonpath='{.items[0].spec.containers[0].resources}'
```

## Common Causes & Fixes

### Cause 1: Missing environment variable / bad Secret reference
```
Error: Cannot connect to database: no host provided
```
**Fix:**
```bash
# Verify secret exists and has correct keys
kubectl get secret shopsphere-secrets -n shopsphere-dev -o yaml
# Re-apply if missing
kubectl apply -f k8s/base/secrets.yaml -n shopsphere-dev
```

### Cause 2: Database not ready (Spring Boot startup race)
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```
**Fix:** Increase startupProbe failureThreshold or add an initContainer:
```yaml
initContainers:
  - name: wait-for-postgres
    image: busybox:1.36
    command: ['sh', '-c', 'until nc -z postgres 5432; do sleep 2; done']
```

### Cause 3: OOMKilled — memory limit too low
```bash
kubectl describe pod ... | grep -A5 "Last State"
# Shows: Reason: OOMKilled
```
**Fix:** Increase memory limit in values.yaml or k8s/base/deployments.yaml

### Cause 4: Readiness probe failing during startup
```
Readiness probe failed: HTTP probe failed with statuscode: 503
```
**Fix:** The startupProbe should give 150s before liveness/readiness kick in.
Verify startupProbe failureThreshold=30 and periodSeconds=5 in the Deployment.

## Reproduce the Lab

```bash
# Intentionally break a secret key to trigger CrashLoopBackOff
kubectl patch secret shopsphere-secrets -n shopsphere-dev \
  --type='json' -p='[{"op":"remove","path":"/data/POSTGRES_PASSWORD"}]'

# Watch pods crash
kubectl get pods -n shopsphere-dev -w

# Restore the secret
kubectl apply -f k8s/base/secrets.yaml -n shopsphere-dev
```
