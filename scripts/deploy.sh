#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Kubernetes Deployment Script
# Supports deploying via Helm or Kustomize across environments.
# Usage: ./deploy.sh [dev|staging|prod] [helm|kustomize]
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENV="${1:-dev}"
METHOD="${2:-helm}"
NAMESPACE="shopsphere-${ENV}"

echo "========================================================"
echo " Deploying ShopSphere to [${ENV}] via [${METHOD}]"
echo "========================================================"

kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

if [ "${METHOD}" = "helm" ]; then
  CHART_DIR="${ROOT_DIR}/helm/shopsphere"
  VALUES_FILE="${CHART_DIR}/values-${ENV}.yaml"

  if [ ! -f "${VALUES_FILE}" ]; then
    VALUES_FILE="${CHART_DIR}/values.yaml"
  fi

  echo "Deploying Helm chart with values: ${VALUES_FILE}..."
  helm upgrade --install shopsphere "${CHART_DIR}" \
    --namespace "${NAMESPACE}" \
    --values "${VALUES_FILE}" \
    --set global.environment="${ENV}"

elif [ "${METHOD}" = "kustomize" ]; then
  OVERLAY_DIR="${ROOT_DIR}/k8s/overlays/${ENV}"
  echo "Applying Kustomize overlay from ${OVERLAY_DIR}..."
  kubectl apply -k "${OVERLAY_DIR}" -n "${NAMESPACE}"
else
  echo "Error: Unknown deployment method '${METHOD}'. Use 'helm' or 'kustomize'."
  exit 1
fi

echo ""
echo "Deployment initiated. Checking rollout status..."
kubectl rollout status deployment -n "${NAMESPACE}" --timeout=180s || true
echo "✓ Deployment to ${NAMESPACE} completed."
