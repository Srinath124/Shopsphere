# Kubernetes Deployment Guide
## ShopSphere / Go N Buy Platform

### 1. Local Kubernetes Deployment via Kind

The project includes an automated script to spin up a multi-node Kind cluster with NGINX Ingress Controller:

```bash
chmod +x scripts/kind-setup.sh
./scripts/kind-setup.sh
```

---

### 2. Deploying via Helm

Deploying to `dev` environment:
```bash
./scripts/deploy.sh dev helm
```

Deploying to `prod` environment:
```bash
./scripts/deploy.sh prod helm
```

#### Verifying Deployment:
```bash
kubectl get pods -n shopsphere-dev
kubectl get ingress -n shopsphere-dev
kubectl get hpa -n shopsphere-prod
```

---

### 3. Deploying via Kustomize Overlays

If deploying without Helm:
```bash
# Dev overlay
kubectl apply -k k8s/overlays/dev

# Prod overlay
kubectl apply -k k8s/overlays/prod
```

---

### 4. Rollback Strategy
```bash
# Rollback via Helm
helm rollback shopsphere -n shopsphere-prod

# Rollback individual microservice deployment
kubectl rollout undo deployment/order-service -n shopsphere-prod
```
