#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Kind Cluster Setup Script
# Creates a multi-node local Kubernetes cluster with NGINX Ingress Controller.
# =============================================================================
set -euo pipefail

CLUSTER_NAME="shopsphere-cluster"

echo "Creating Kind cluster configuration..."
cat <<EOF > /tmp/kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
EOF

echo "Spinning up Kind cluster: ${CLUSTER_NAME}..."
kind create cluster --name "${CLUSTER_NAME}" --config /tmp/kind-config.yaml

echo "Installing NGINX Ingress Controller..."
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

echo "Waiting for Ingress controller pods to be ready..."
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "Creating ShopSphere namespaces..."
kubectl create namespace shopsphere-dev || true
kubectl create namespace shopsphere-prod || true
kubectl create namespace monitoring || true

echo "✓ Kind cluster is ready for ShopSphere deployments!"
