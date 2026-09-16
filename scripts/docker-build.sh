#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Docker Image Build Script
# Builds and tags all microservices and frontend images.
# =============================================================================
set -euo pipefail

REGISTRY="${CONTAINER_REGISTRY:-shopsphere}"
TAG="${IMAGE_TAG:-latest}"

SERVICES=(
  "auth-service"
  "product-service"
  "cart-service"
  "inventory-service"
  "order-service"
  "notification-service"
)

echo "Building all Docker images with tag: ${TAG}"

for SERVICE in "${SERVICES[@]}"; do
  echo "Building ${REGISTRY}/${SERVICE}:${TAG}..."
  docker build -t "${REGISTRY}/${SERVICE}:${TAG}" "./services/${SERVICE}"
done

echo "Building frontend ${REGISTRY}/frontend:${TAG}..."
docker build -t "${REGISTRY}/frontend:${TAG}" "./frontend"

echo "✓ All Docker images built successfully."
