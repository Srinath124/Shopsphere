# Troubleshooting Lab 4: Database Failure

## Symptom

Spring Boot services fail to start or crash with database connection errors:
```
HikariPool-1 - Connection is not available, request timed out after 30000ms.
org.postgresql.util.PSQLException: The connection attempt failed.
```

## Diagnosis Steps

```bash
# 1. Check if Postgres pod is running (Docker Compose)
docker compose ps postgres
docker compose logs postgres --tail=50

# 2. Check if Postgres is reachable (Docker Compose)
docker compose exec postgres pg_isready -U shopsphere

# 3. Check per-service database existence
docker compose exec postgres psql -U shopsphere -l | grep "_db"

# 4. Check for Kubernetes (Kind)
kubectl get pods -n shopsphere-dev -l app=postgres  # if deployed in k8s
# Or check external DB endpoint via ConfigMap
kubectl describe configmap shopsphere-config -n shopsphere-dev | grep POSTGRES

# 5. Test DB connection from within pod
kubectl exec -it deployment/auth-service -n shopsphere-dev -- \
  sh -c "nc -zv \$POSTGRES_HOST \$POSTGRES_PORT && echo 'DB reachable' || echo 'DB unreachable'"
```

## Common Causes & Fixes

### Cause 1: Database container not initialized yet
```bash
# Check postgres startup logs
docker compose logs postgres | grep "database system is ready"

# Wait for readiness
until docker compose exec postgres pg_isready -U shopsphere; do
  echo "Waiting for postgres..."; sleep 2
done
```

### Cause 2: Wrong database URL / credentials
```bash
# Verify environment variables in running container
kubectl exec -it deployment/auth-service -n shopsphere-dev -- \
  printenv | grep -E 'POSTGRES|DATASOURCE'

# Expected: SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/auth_db
# If wrong, check ConfigMap and Secret values
```

### Cause 3: Database schema migration failed (Flyway/Liquibase)
```bash
# Check service startup logs for migration errors
kubectl logs -n shopsphere-dev -l app=auth-service | grep -i "flyway\|migration\|error"

# Common fix: Clean and re-run migrations (dev only!)
docker compose exec postgres psql -U shopsphere -d auth_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
kubectl rollout restart deployment/auth-service -n shopsphere-dev
```

### Cause 4: Connection pool exhausted
```bash
# Check active connections
docker compose exec postgres psql -U shopsphere -c \
  "SELECT count(*), state FROM pg_stat_activity GROUP BY state;"

# Increase HikariCP max-pool-size in application.yml
# spring.datasource.hikari.maximum-pool-size=20
```

## Reproduce the Lab

```bash
# Stop postgres (Docker Compose context)
docker compose stop postgres

# Watch auth-service fail health checks
docker compose logs -f auth-service | grep -E "error|FATAL"

# Restart postgres
docker compose start postgres

# Verify services recover automatically
sleep 30
curl http://localhost:8081/actuator/health
```
