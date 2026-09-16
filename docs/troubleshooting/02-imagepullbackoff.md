# Troubleshooting Lab 2: ImagePullBackOff

## Symptom

```
kubectl get pods -n shopsphere-dev
NAME                          READY   STATUS             RESTARTS   AGE
product-service-xxx           0/1     ImagePullBackOff   0          2m
```

## Diagnosis Steps

```bash
# 1. Describe pod for the exact error
kubectl describe pod -n shopsphere-dev -l app=product-service

# Look for "Events" section:
# Failed to pull image "ghcr.io/your-org/product-service:latest":
#   rpc error: code = Unknown desc = failed to pull...
#   unauthorized: authentication required

# 2. Check if image tag exists
# (requires docker or crane CLI)
crane ls ghcr.io/your-org/product-service 2>/dev/null || echo "Registry unreachable or image not found"

# 3. Check imagePullSecrets
kubectl get pod -n shopsphere-dev -l app=product-service \
  -o jsonpath='{.items[0].spec.imagePullSecrets}'
```

## Common Causes & Fixes

### Cause 1: Wrong image tag (tag doesn't exist in registry)
**Fix:** Verify the tag was pushed by CI/CD and update the Deployment:
```bash
# Check the latest available tags
# Using GitHub CLI: gh api /user/packages/container/product-service/versions

# Override image tag
kubectl set image deployment/product-service \
  product-service=ghcr.io/your-org/product-service:sha-abc1234 \
  -n shopsphere-dev
```

### Cause 2: Registry not authenticated (private registry)
**Fix:** Create a registry secret and reference it:
```bash
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=YOUR_GITHUB_USERNAME \
  --docker-password=YOUR_PAT_TOKEN \
  -n shopsphere-dev

# Add to serviceaccount or pod spec:
# imagePullSecrets:
#   - name: ghcr-secret
```

### Cause 3: Kind cluster without pre-loaded image (local dev)
```bash
# Load image directly into Kind cluster
docker pull ghcr.io/your-org/product-service:latest
kind load docker-image ghcr.io/your-org/product-service:latest \
  --name shopsphere-cluster

# Set imagePullPolicy to Never for local images
kubectl patch deployment product-service -n shopsphere-dev \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"product-service","imagePullPolicy":"Never"}]}}}}'
```

## Reproduce the Lab

```bash
# Point to a non-existent image tag
kubectl set image deployment/product-service \
  product-service=ghcr.io/your-org/product-service:nonexistent-tag-99999 \
  -n shopsphere-dev

# Observe ImagePullBackOff
kubectl get pods -n shopsphere-dev -w

# Fix by restoring the correct tag
kubectl set image deployment/product-service \
  product-service=ghcr.io/your-org/product-service:latest \
  -n shopsphere-dev
```
