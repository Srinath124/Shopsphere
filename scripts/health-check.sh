#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — Comprehensive Service Health Check
# =============================================================================
set -uo pipefail

echo "========================================================"
echo " Checking Health of Go N Buy Platform Services"
echo "========================================================"

ENDPOINTS=(
  "auth-service|http://localhost:8081/actuator/health"
  "product-service|http://localhost:8082/actuator/health"
  "cart-service|http://localhost:8083/actuator/health"
  "order-service|http://localhost:8084/actuator/health"
  "inventory-service|http://localhost:8085/actuator/health"
  "notification-service|http://localhost:8086/actuator/health"
)

MAX_RETRIES=15
DELAY_SECONDS=3

all_healthy=true

for item in "${ENDPOINTS[@]}"; do
  IFS="|" read -r name url <<< "$item"
  echo -n "Checking ${name}... "

  retries=0
  success=false

  while [ $retries -lt $MAX_RETRIES ]; do
    status=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    if [ "$status" = "200" ]; then
      echo "✓ HEALTHY (HTTP 200)"
      success=true
      break
    fi
    retries=$((retries + 1))
    sleep $DELAY_SECONDS
  done

  if [ "$success" = "false" ]; then
    echo "✗ UNHEALTHY / TIMED OUT (Status: ${status})"
    all_healthy=false
  fi
done

echo "--------------------------------------------------------"
if [ "$all_healthy" = "true" ]; then
  echo "✓ All backend microservices are reporting HEALTHY."
  exit 0
else
  echo "⚠ Some services failed the health check."
  exit 1
fi
