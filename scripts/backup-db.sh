#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — PostgreSQL Database Backup Script
# =============================================================================
set -euo pipefail

BACKUP_DIR="${1:-./backups}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p "${BACKUP_DIR}"

DATABASES=("auth_db" "product_db" "order_db" "inventory_db" "notification_db")
CONTAINER_NAME="shopsphere-postgres"

echo "Backing up databases from ${CONTAINER_NAME}..."

for db in "${DATABASES[@]}"; do
  OUTPUT_FILE="${BACKUP_DIR}/${db}_${TIMESTAMP}.sql.gz"
  echo "  Dumping ${db} -> ${OUTPUT_FILE}..."
  docker exec -t "${CONTAINER_NAME}" pg_dump -U shopsphere -d "${db}" | gzip > "${OUTPUT_FILE}"
  echo "  ✓ Backup complete: $(du -h "${OUTPUT_FILE}" | cut -f1)"
done

echo "All databases backed up to ${BACKUP_DIR}"
