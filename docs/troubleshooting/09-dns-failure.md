# Troubleshooting Lab 9: DNS Failure

## Symptom

Services cannot resolve each other by hostname:
```
java.net.UnknownHostException: inventory-service: Name or service not known
```

## Diagnosis Steps

```bash
# 1. Test DNS resolution from within the cluster
kubectl run dnstest --image=busybox:1.36 --restart=Never -n shopsphere-dev \
  -- sh -c "nslookup inventory-service && nslookup inventory-service.shopsphere-dev.svc.cluster.local"
kubectl logs dnstest -n shopsphere-dev
kubectl delete pod dnstest -n shopsphere-dev

# 2. Check CoreDNS pods are running
kubectl get pods -n kube-system -l k8s-app=kube-dns

# 3. Check CoreDNS logs for errors
kubectl logs -n kube-system -l k8s-app=kube-dns --tail=50

# 4. Check Service exists in the correct namespace
kubectl get svc -n shopsphere-dev | grep inventory

# 5. Check ndots configuration (affects which DNS names are tried)
kubectl exec -it deployment/inventory-service -n shopsphere-dev -- \
  cat /etc/resolv.conf
```

## Common Causes & Fixes

### Cause 1: Service doesn't exist in the namespace
```bash
# Verify service exists
kubectl get svc inventory-service -n shopsphere-dev

# If missing, apply:
kubectl apply -f k8s/base/deployments.yaml -n shopsphere-dev
```

### Cause 2: Wrong namespace (Kustomize namePrefix changes names)
```bash
# In dev overlay, namePrefix: dev- changes names to dev-inventory-service
# The ConfigMap INVENTORY_SERVICE_URL must match

kubectl get svc -n shopsphere-dev | grep inventory
# → dev-inventory-service (not inventory-service!)

# Fix: Set CONFIG_MAP URL to match the prefixed name
# Or remove namePrefix from dev overlay kustomization
```

### Cause 3: CoreDNS not running / pod evicted
```bash
# Restart CoreDNS if not running
kubectl rollout restart deployment/coredns -n kube-system
kubectl rollout status deployment/coredns -n kube-system

# For Kind clusters:
kind get clusters
docker exec -it shopsphere-cluster-control-plane bash -c \
  "kubectl get pods -n kube-system | grep dns"
```

### Cause 4: Service FQDN required (cross-namespace)
```bash
# If order-service in namespace A calls inventory-service in namespace B:
# Must use FQDN: inventory-service.shopsphere-dev.svc.cluster.local

# Fix: Update ConfigMap
kubectl patch configmap shopsphere-config -n shopsphere-dev \
  --type=merge \
  -p '{"data":{"INVENTORY_SERVICE_URL":"http://inventory-service.shopsphere-dev.svc.cluster.local:8085"}}'
```

## Reproduce the Lab

```bash
# 1. Delete the inventory-service Service object
kubectl delete svc inventory-service -n shopsphere-dev

# 2. Test DNS resolution — should fail
kubectl run dnstest --image=busybox:1.36 --restart=Never -n shopsphere-dev \
  -- sh -c "nslookup inventory-service"
kubectl logs dnstest -n shopsphere-dev
kubectl delete pod dnstest -n shopsphere-dev

# 3. Restore the service
kubectl apply -f k8s/base/deployments.yaml -n shopsphere-dev

# 4. Verify DNS works again
kubectl run dnstest2 --image=busybox:1.36 --restart=Never -n shopsphere-dev \
  -- sh -c "nslookup inventory-service"
kubectl logs dnstest2 -n shopsphere-dev
kubectl delete pod dnstest2 -n shopsphere-dev
```
