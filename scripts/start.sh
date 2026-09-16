#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Stack Startup Script
# Starts Docker Compose stack with optional observability support.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENABLE_OBSERVABILITY="${1:-false}"

echo "========================================================"
echo " Launching Go N Buy Platform via Docker Compose"
echo "========================================================"

cd "${ROOT_DIR}"

if [ ! -f ".env" ]; then
  echo "No .env found. Generating from .env.example..."
  cp .env.example .env
fi

echo "Starting core infrastructure, microservices, and gateway..."
docker compose -f docker-compose.yml up -d --build

if [ "${ENABLE_OBSERVABILITY}" = "true" ] || [ "${ENABLE_OBSERVABILITY}" = "--with-monitoring" ]; then
  echo "Starting observability stack (Prometheus, Grafana, Loki, OTel)..."
  docker compose -f docker-compose.observability.yml up -d
fi

echo ""
echo "Waiting for services to become healthy..."
"${SCRIPT_DIR}/health-check.sh" || true

echo ""
echo "========================================================"
echo " Go N Buy Platform is LIVE!"
echo " Frontend:      http://localhost"
echo " API Gateway:   http://localhost/api"
echo " RabbitMQ UI:   http://localhost:15672 (guest / guest)"
if [ "${ENABLE_OBSERVABILITY}" = "true" ] || [ "${ENABLE_OBSERVABILITY}" = "--with-monitoring" ]; then
  echo " Prometheus:    http://localhost:9090"
  echo " Grafana:       http://localhost:3001 (admin / admin)"
fi
echo "========================================================"
