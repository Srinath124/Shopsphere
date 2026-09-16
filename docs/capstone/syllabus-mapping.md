# DevOps Competency Syllabus Mapping
## ShopSphere / Go N Buy Platform

This document maps all repository artifacts to modern DevOps, Cloud, and SRE curriculum learning outcomes.

---

| Core Competency Area | Capstone Component / Implementation | Key Artifacts in Repository |
| :--- | :--- | :--- |
| **1. Cloud-Native Microservices** | 6 decoupled Spring Boot 3 services with Java 21, JPA/Hibernate, Flyway migrations | `services/*` |
| **2. Containerization** | Multi-stage Docker builds, Alpine minimal runtimes, non-root users, healthchecks | `services/*/Dockerfile`, `frontend/Dockerfile` |
| **3. Container Orchestration** | Kubernetes Deployments, Services, ConfigMaps, Secrets, Ingress, HPAs, Kustomize | `k8s/base/*`, `k8s/overlays/*` |
| **4. Package Management** | Parameterized Helm v3 charts with dev/prod values overrides and label helpers | `helm/shopsphere/*` |
| **5. Infrastructure as Code (IaC)** | Modular Terraform provisioning AWS VPC, EKS, RDS PostgreSQL, ElastiCache Redis, IRSA | `terraform/modules/*`, `terraform/environments/*` |
| **6. Configuration Management** | Ansible playbooks & roles for server hardening, UFW firewall, Docker CE, Node Exporter | `ansible/*` |
| **7. CI/CD Automation** | GitHub Actions matrix builds, container image publishing to GHCR, automated Helm deployments | `.github/workflows/ci.yml`, `cd.yml` |
| **8. Security & Compliance** | Trivy filesystem & container scans, Checkov IaC scanning, BCrypt password hashing, JWT stateless auth | `.github/workflows/security.yml` |
| **9. Observability & Monitoring** | Prometheus metrics scraping, custom SLO alert rules, Grafana dashboards, Loki, OpenTelemetry | `monitoring/*`, `docker-compose.observability.yml` |
| **10. Site Reliability Eng (SRE)** | Formal SLO/SLI specifications, Error Budget Policy, P1/P2 runbooks, Chaos experiments | `sre/*` |
| **11. Cloud Financial Mgmt (FinOps)**| Cost allocation tagging, Graviton architecture, idle dev shutdowns, monthly cost estimator | `finops/*` |
| **12. AIOps & ML Telemetry** | Rolling Z-score anomaly detection engine analyzing Prometheus metrics in real time | `aiops/anomaly-detector.py` |
| **13. Performance & Load Testing** | k6 browsing load tests, checkout stress tests, and flash-sale race condition concurrency tests | `tests/load/*` |
