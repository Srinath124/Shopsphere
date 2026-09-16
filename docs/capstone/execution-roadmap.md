# Capstone Execution Roadmap & Milestones
## ShopSphere / Go N Buy Platform

This roadmap documents the 8-week production engineering delivery milestones from initial scaffold to production-ready multi-cloud deployment.

---

### Milestone Schedule

```mermaid
gantt
    title DevOps Capstone Delivery Roadmap
    dateFormat  YYYY-MM-DD
    section Backend & Core
    Domain Modeling & Flyway Migrations :done, m1, 2026-08-01, 7d
    6 Spring Boot Microservices Dev     :done, m2, 2026-08-08, 14d
    RabbitMQ & Redis Integration        :done, m3, 2026-08-15, 7d
    section Frontend & Gateway
    React Vite TS SPA & AuthContext    :done, m4, 2026-08-22, 7d
    NGINX Reverse Proxy & API Gateway   :done, m5, 2026-08-25, 4d
    section Automation & Cloud
    Docker Compose Multi-Container      :done, m6, 2026-08-29, 5d
    Terraform AWS Infrastructure (EKS)  :done, m7, 2026-09-03, 7d
    Kubernetes Helm Charts & Overlays   :done, m8, 2026-09-08, 5d
    section SRE & Production
    Prometheus & Grafana Dashboards     :done, m9, 2026-09-10, 3d
    k6 Load & Flash-Sale Race Tests     :done, m10, 2026-09-12, 2d
    AIOps Statistical Anomaly Detector  :done, m11, 2026-09-14, 2d
```

---

### Deliverable Verification Matrix

| Milestone | Deliverable | Status | Verification Command |
| :--- | :--- | :--- | :--- |
| **M1: Backend Services** | 6 Maven microservices (Java 21) | Complete | `mvn clean test` across all services |
| **M2: Data Layer** | PostgreSQL per service + Redis Cart | Complete | `docker compose -f docker-compose.dev.yml up -d` |
| **M3: Event Broker** | RabbitMQ topic exchange & DLQ | Complete | Verification via RabbitMQ UI (`:15672`) |
| **M4: Web Frontend** | React + Vite + TypeScript SPA | Complete | `npm run build` in `frontend/` |
| **M5: IaC** | Terraform VPC, EKS, RDS, Redis, IAM | Complete | `terraform validate` in `terraform/environments/dev` |
| **M6: K8s / Helm** | Helm chart + Kustomize dev/prod | Complete | `helm lint ./helm/shopsphere` |
| **M7: CI/CD & Sec** | GitHub Actions workflows | Complete | Trivy & Checkov automated scans passing |
| **M8: Observability** | Prometheus, Grafana, Loki, OTel | Complete | Dashboards visual on port 3001 |
| **M9: SRE & AIOps** | Anomaly detector & Load tests | Complete | `python aiops/anomaly-detector.py` & k6 execution |
