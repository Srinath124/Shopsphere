# 🛒 Go N Buy — Production DevOps Capstone Project

> A production-grade E-Commerce Microservices Platform demonstrating modern DevOps, cloud-native architecture, and SRE practices.

[![CI](https://github.com/your-org/shopsphere/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/shopsphere/actions)
[![Security](https://github.com/your-org/shopsphere/actions/workflows/security.yml/badge.svg)](https://github.com/your-org/shopsphere/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Technology Stack](#technology-stack)
5. [Prerequisites](#prerequisites)
6. [Local Setup](#local-setup)
7. [Docker Setup](#docker-setup)
8. [Kubernetes Setup](#kubernetes-setup)
9. [Helm Setup](#helm-setup)
10. [Terraform / AWS Setup](#terraform--aws-setup)
11. [CI/CD](#cicd)
12. [Security](#security)
13. [Observability](#observability)
14. [SRE](#sre)
15. [FinOps](#finops)
16. [AIOps](#aiops)
17. [Troubleshooting](#troubleshooting)

---

## Project Overview

**Go N Buy** is a fully production-style e-commerce microservices platform built as a DevOps capstone project. It demonstrates:

- **Microservices architecture** with 6 independent Spring Boot services
- **Event-driven communication** via RabbitMQ
- **Container orchestration** via Kubernetes (Kind locally, EKS on AWS)
- **Full CI/CD** via GitHub Actions
- **Infrastructure as Code** via Terraform
- **Configuration management** via Ansible
- **Complete observability** via Prometheus + Grafana + Loki + OpenTelemetry
- **SRE practices** with SLOs, error budgets, and incident management
- **DevSecOps** with Trivy image scanning and security pipelines
- **FinOps** cost modeling and optimization strategies
- **AIOps** anomaly detection

---

## Architecture

```
                         INTERNET
                            │
                            ▼
                    NGINX INGRESS CONTROLLER
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
        PRODUCT API     CART API       ORDER API
             │              │              │
             ▼              ▼              ▼
        PostgreSQL        Redis        PostgreSQL
                                           │
                                           ▼
                                     INVENTORY API
                                           │
                                           ▼
                                      PostgreSQL

AUTH SERVICE ──► PostgreSQL

ORDER SERVICE ──► INVENTORY SERVICE (REST)
ORDER SERVICE ──► RABBITMQ ──► NOTIFICATION SERVICE ──► PostgreSQL

All Services ──► Prometheus metrics
             ──► Structured JSON logs ──► Loki
             ──► OpenTelemetry traces
```

See [docs/architecture.md](docs/architecture.md) for detailed diagrams.

---

## Features

### Backend Services
| Service | Responsibility | Database | Port |
|---------|---------------|----------|------|
| auth-service | Authentication, JWT, users | PostgreSQL | 8081 |
| product-service | Products, categories, search | PostgreSQL | 8082 |
| cart-service | Shopping cart management | Redis | 8083 |
| order-service | Order lifecycle, payments | PostgreSQL | 8084 |
| inventory-service | Stock management, reservations | PostgreSQL | 8085 |
| notification-service | Event consumption, notifications | PostgreSQL | 8086 |

### Frontend
- React + TypeScript + Vite
- Complete e-commerce UI (browse, cart, checkout, orders)
- Admin dashboard (product management, inventory)

### Infrastructure
- Docker Compose for local development
- Kubernetes with Kind for local orchestration
- Terraform for AWS EKS infrastructure
- Ansible for server configuration
- GitHub Actions CI/CD pipeline

---

## Technology Stack

| Category | Technology |
|----------|-----------|
| Backend | Java 21, Spring Boot 3.x, Maven |
| Frontend | React, TypeScript, Vite |
| Databases | PostgreSQL, Redis |
| Messaging | RabbitMQ |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes, Kind |
| Packaging | Helm, Kustomize |
| Ingress | NGINX Ingress Controller |
| CI/CD | GitHub Actions |
| Registry | GitHub Container Registry (GHCR) |
| IaC | Terraform |
| Config Mgmt | Ansible |
| Monitoring | Prometheus, Grafana |
| Logging | Loki, Promtail |
| Tracing | OpenTelemetry |
| Load Testing | k6 |
| Security | Trivy, JWT, BCrypt |
| Cloud | AWS (EKS, RDS, ElastiCache) |

---

## Prerequisites

```bash
# Required tools
java --version          # Java 21+
mvn --version           # Maven 3.9+
docker --version        # Docker 24+
docker compose version  # Docker Compose v2+
node --version          # Node.js 20+
npm --version           # npm 10+
kubectl version         # kubectl 1.28+
kind --version          # Kind 0.20+
helm version            # Helm 3.12+
terraform --version     # Terraform 1.6+
ansible --version       # Ansible 2.15+
git --version           # Git 2.40+
```

---

## Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-org/shopsphere.git
cd shopsphere

# 2. Copy environment configuration
cp .env.example .env
# Edit .env with your local values (defaults work for Docker Compose)

# 3. Start everything with Docker Compose
docker compose up --build

# 4. Access the application
# Frontend:    http://localhost:3000
# Auth API:    http://localhost:8081/swagger-ui.html
# Product API: http://localhost:8082/swagger-ui.html
# Cart API:    http://localhost:8083/swagger-ui.html
# Order API:   http://localhost:8084/swagger-ui.html
# Inventory:   http://localhost:8085/swagger-ui.html
# Notify:      http://localhost:8086/swagger-ui.html
# Grafana:     http://localhost:3001 (admin/admin)
# Prometheus:  http://localhost:9090
# RabbitMQ:    http://localhost:15672 (guest/guest)
```

See [docs/local-development.md](docs/local-development.md) for detailed instructions.

---

## Docker Setup

```bash
# Build all images
./scripts/docker-build.sh

# Start with Docker Compose
./scripts/start-local.sh

# Stop
./scripts/stop-local.sh

# Clean all containers and volumes
./scripts/clean-local.sh
```

See [docs/docker.md](docs/docker.md).

---

## Kubernetes Setup

```bash
# Create Kind cluster
./scripts/kind-create.sh

# Deploy to Kind
./scripts/deploy-local.sh

# Add to /etc/hosts (Linux/macOS) or C:\Windows\System32\drivers\etc\hosts (Windows)
# 127.0.0.1 shopsphere.local

# Access: http://shopsphere.local
```

See [docs/kubernetes.md](docs/kubernetes.md).

---

## Helm Setup

```bash
# Install with Helm (dev environment)
helm install shopsphere helm/shopsphere -f helm/shopsphere/values-dev.yaml -n shopsphere --create-namespace

# Upgrade
helm upgrade shopsphere helm/shopsphere -f helm/shopsphere/values-dev.yaml -n shopsphere

# Lint
helm lint helm/shopsphere

# Dry run
helm template shopsphere helm/shopsphere -f helm/shopsphere/values-dev.yaml
```

See [docs/helm.md](docs/helm.md).

---

## Terraform / AWS Setup

```bash
cd terraform/environments/dev

# Initialize
terraform init -backend-config=backend.hcl

# Plan
terraform plan -var-file=terraform.tfvars

# Apply
terraform apply -var-file=terraform.tfvars
```

See [docs/terraform.md](docs/terraform.md).

---

## CI/CD

GitHub Actions workflows:
- **ci.yml** — Build, test, security scan on every PR
- **security.yml** — Trivy scanning on schedule + push
- **cd.yml** — Build, push to GHCR, deploy to Kubernetes

See [docs/ci-cd.md](docs/ci-cd.md).

---

## Security

- JWT authentication with refresh tokens
- BCrypt password hashing
- Role-based authorization (USER, ADMIN)
- Trivy container image scanning
- Non-root Docker containers
- Kubernetes NetworkPolicies
- Kubernetes RBAC
- Secrets via environment variables (never committed)

See [docs/security.md](docs/security.md).

---

## Observability

```bash
# Start observability stack
docker compose -f docker-compose.observability.yml up

# Grafana: http://localhost:3001 (admin/admin)
# Prometheus: http://localhost:9090
# Loki: http://localhost:3100
```

Dashboards:
1. Application Overview
2. Kubernetes Overview
3. JVM Metrics
4. HTTP Metrics
5. E-Commerce Business Metrics
6. SRE Dashboard

See [docs/observability.md](docs/observability.md).

---

## SRE

- **Availability SLO**: 99.9% (error budget: 8.76 hours/year)
- **Latency SLO**: 95th percentile < 300ms
- **Order success SLO**: 99%

See [sre/slo.md](sre/slo.md) and [docs/sre.md](docs/sre.md).

---

## FinOps

Cost model, optimization strategies, and right-sizing guidance.

See [finops/README.md](finops/README.md).

---

## AIOps

Anomaly detection for CPU, memory, latency, and error rates.

See [aiops/README.md](aiops/README.md).

---

## Troubleshooting

See [docs/troubleshooting.md](docs/troubleshooting.md) for 10 detailed failure scenarios with symptoms, investigation commands, root causes, fixes, and verification steps.

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [docs/architecture.md](docs/architecture.md) | System architecture and diagrams |
| [docs/local-development.md](docs/local-development.md) | Local setup guide |
| [docs/docker.md](docs/docker.md) | Docker guide |
| [docs/kubernetes.md](docs/kubernetes.md) | Kubernetes guide |
| [docs/helm.md](docs/helm.md) | Helm guide |
| [docs/kustomize.md](docs/kustomize.md) | Kustomize guide |
| [docs/terraform.md](docs/terraform.md) | Terraform/AWS guide |
| [docs/ansible.md](docs/ansible.md) | Ansible guide |
| [docs/ci-cd.md](docs/ci-cd.md) | CI/CD pipeline |
| [docs/security.md](docs/security.md) | Security practices |
| [docs/observability.md](docs/observability.md) | Observability stack |
| [docs/logging.md](docs/logging.md) | Centralized logging |
| [docs/tracing.md](docs/tracing.md) | Distributed tracing |
| [docs/autoscaling.md](docs/autoscaling.md) | HPA and autoscaling |
| [docs/troubleshooting.md](docs/troubleshooting.md) | Troubleshooting guide |
| [docs/sre.md](docs/sre.md) | SRE practices |
| [docs/finops.md](docs/finops.md) | FinOps practices |
| [docs/aiops.md](docs/aiops.md) | AIOps guide |
| [docs/git-workflow.md](docs/git-workflow.md) | Git workflow |
| [docs/backup-restore.md](docs/backup-restore.md) | Backup and restore |
| [docs/execution-roadmap.md](docs/execution-roadmap.md) | Step-by-step execution |
| [docs/final-demo.md](docs/final-demo.md) | Final demo walkthrough |
| [docs/syllabus-mapping.md](docs/syllabus-mapping.md) | Learning topic mapping |

---

## License

MIT License — see [LICENSE](LICENSE).

---

*Built as a production-grade DevOps capstone project.*
