#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Stack Shutdown Script
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "Shutting down Go N Buy stack..."
cd "${ROOT_DIR}"

if docker compose -f docker-compose.observability.yml ps -q 2>/dev/null | grep -q .; then
  echo "Stopping observability stack..."
  docker compose -f docker-compose.observability.yml down
fi

docker compose -f docker-compose.yml down

echo "✓ All platform services stopped successfully."
