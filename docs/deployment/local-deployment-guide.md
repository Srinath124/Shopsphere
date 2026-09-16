# Local Development & Deployment Guide
## ShopSphere / Go N Buy Platform

### 1. Prerequisites
- Docker & Docker Compose v2.20+
- JDK 21 (Eclipse Temurin recommended)
- Maven 3.9+
- Node.js 20+ & npm

---

### 2. Quickstart with Docker Compose (Recommended)

To run the entire platform (6 microservices, PostgreSQL, Redis, RabbitMQ, React frontend, and NGINX):

```bash
# 1. Clone & enter project root
cd shopsphere

# 2. Prepare environment variables
cp .env.example .env

# 3. Start platform stack
chmod +x scripts/*.sh
./scripts/start.sh

# Or with full observability (Prometheus, Grafana, Loki):
./scripts/start.sh --with-monitoring
```

#### Accessing Services:
- **Frontend Web Application**: [http://localhost](http://localhost)
- **API Gateway**: [http://localhost/api](http://localhost/api)
- **RabbitMQ Management Dashboard**: [http://localhost:15672](http://localhost:15672) (Credentials: `guest` / `guest`)
- **Prometheus Dashboard**: [http://localhost:9090](http://localhost:9090)
- **Grafana Metrics**: [http://localhost:3001](http://localhost:3001) (Credentials: `admin` / `admin`)

---

### 3. Local Development Mode (Backing Services in Docker, Code Running on Host)

If you are modifying Java or React code and want hot-reloading:

```bash
# 1. Start only backing databases and message brokers
docker compose -f docker-compose.dev.yml up -d

# 2. Run backend services individually (e.g. auth-service)
cd services/auth-service
mvn spring-boot:run

# 3. In another terminal, run frontend
cd frontend
npm install
npm run dev
```
Frontend dev server will be live at `http://localhost:3000`.

---

### 4. Stopping the Stack
```bash
./scripts/stop.sh
```
