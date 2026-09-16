#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Comprehensive Build Script
# Builds all 6 Spring Boot microservices and React frontend.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "========================================================"
echo " Starting Full Platform Build: Go N Buy (ShopSphere)"
echo "========================================================"

SERVICES=(
  "auth-service"
  "product-service"
  "cart-service"
  "inventory-service"
  "order-service"
  "notification-service"
)

# 1. Build Spring Boot Microservices
echo ""
echo ">>> [1/2] Building Spring Boot Microservices (Maven)..."
for SERVICE in "${SERVICES[@]}"; do
  SERVICE_DIR="${ROOT_DIR}/services/${SERVICE}"
  if [ -d "${SERVICE_DIR}" ]; then
    echo "  -> Packaging ${SERVICE}..."
    (cd "${SERVICE_DIR}" && mvn clean package -DskipTests -q)
    echo "  ✓ ${SERVICE} build completed successfully."
  else
    echo "  [WARN] Service directory ${SERVICE_DIR} not found!"
  fi
done

# 2. Build React Frontend
echo ""
echo ">>> [2/2] Building React + Vite Frontend..."
FRONTEND_DIR="${ROOT_DIR}/frontend"
if [ -d "${FRONTEND_DIR}" ]; then
  (cd "${FRONTEND_DIR}" && npm install --silent && npm run build)
  echo "  ✓ Frontend built successfully into dist/"
else
  echo "  [WARN] Frontend directory ${FRONTEND_DIR} not found!"
fi

echo ""
echo "========================================================"
echo " Build Completed Successfully for All Platform Modules! "
echo "========================================================"
