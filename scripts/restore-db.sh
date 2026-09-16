#!/usr/bin/env bash
# =============================================================================
# ShopSphere / Go N Buy — PostgreSQL Database Restore Script
# Usage: ./restore-db.sh <backup_file_path> <database_name>
# =============================================================================
set -euo pipefail

BACKUP_FILE="${1:-}"
DB_NAME="${2:-}"
CONTAINER_NAME="shopsphere-postgres"

if [ -z "${BACKUP_FILE}" ] || [ -z "${DB_NAME}" ]; then
  echo "Usage: $0 <path_to_backup_file.sql.gz> <target_database_name>"
  exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "Error: Backup file not found: ${BACKUP_FILE}"
  exit 1
fi

echo "Restoring ${BACKUP_FILE} into ${DB_NAME} on container ${CONTAINER_NAME}..."
gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER_NAME}" psql -U shopsphere -d "${DB_NAME}"

echo "✓ Database ${DB_NAME} restored successfully."
