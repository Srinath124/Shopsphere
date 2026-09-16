# Go N Buy (ShopSphere) — Complete Project Structure & Codebase Map

> **Production-grade E-Commerce Microservices Platform**  
> Complete DevOps Capstone Architecture, Source Code, Infrastructure, CI/CD, Kubernetes, SRE, FinOps, and AIOps.

---

## 1. Complete Filesystem Tree

```
d:\go N buy\shopsphere\
│
├── .github/                                # GitHub Actions CI/CD & Automation
│   └── workflows/
│       ├── ci.yml                          # Microservices test matrix & frontend build
│       ├── cd.yml                          # Multi-image GHCR publish & Helm deploy
│       └── security.yml                    # Trivy vulnerability & Checkov IaC security scan
│
├── aiops/                                  # AIOps Telemetry & ML Anomaly Detection
│   └── anomaly-detector.py                 # Prometheus scraper & rolling Z-score anomaly detector
│
├── ansible/                                # Ansible Configuration Management
│   ├── ansible.cfg                         # Ansible runtime configuration & privilege escalation
│   ├── inventory/
│   │   └── hosts.ini                       # Control plane & worker node inventory
│   ├── playbooks/
│   │   ├── site.yml                        # Master provisioning playbook
│   │   └── docker.yml                      # Container runtime provisioning
│   └── roles/
│       ├── common/                         # Base packages, UTC timezone, sysctl k8s tuning
│       │   └── tasks/main.yml
│       ├── security/                       # UFW firewall, fail2ban, SSH hardening
│       │   ├── tasks/main.yml
│       │   └── handlers/main.yml
│       ├── docker/                         # Docker CE, containerd, systemd cgroup driver
│       │   ├── tasks/main.yml
│       │   └── handlers/main.yml
│       └── monitoring_agent/               # Prometheus Node Exporter agent setup
│           └── tasks/main.yml
│
├── docker/                                 # Docker Container Assets
│   ├── nginx/
│   │   └── nginx.conf                      # Edge reverse proxy / API gateway routing
│   └── postgres/
│       ├── init-databases.sh               # Multi-database bash entrypoint script
│       └── init-databases.sql              # SQL fallback database creation & grants
│
├── docs/                                   # Architectural, Operational & Capstone Docs
│   ├── architecture/
│   │   ├── system-architecture.md          # Architecture & Mermaid interaction diagrams
│   │   └── data-flow.md                    # Pessimistic locking & Idempotency key mechanics
│   ├── deployment/
│   │   ├── local-deployment-guide.md       # Docker Compose & native local runbook
│   │   ├── kubernetes-deployment-guide.md  # Kind, Minikube, and EKS deployment
│   │   ├── terraform-guide.md              # AWS infrastructure provisioning instructions
│   │   └── terraform-remote-state.md       # S3 backend & DynamoDB state locking architecture
│   ├── troubleshooting/                    # Production incident & debugging runbooks (10 Labs)
│   │   ├── 01-crashloopbackoff.md          # Memory limits & Flyway lock resolution
│   │   ├── 02-imagepullbackoff.md          # Registry credentials & tag mismatch
│   │   ├── 03-service-connection-failure.md # Kube-DNS & Service selector alignment
│   │   ├── 04-database-failure.md          # HikariCP pool exhaustion & PostgreSQL max connections
│   │   ├── 05-redis-failure.md             # Cache eviction policies & failover
│   │   ├── 06-rabbitmq-failure.md          # Consumer starvation & DLQ routing
│   │   ├── 07-readiness-failure.md         # Flapping actuator probes & health endpoints
│   │   ├── 08-high-cpu.md                  # Thread starvation & HPA thrashing
│   │   ├── 09-dns-failure.md               # CoreDNS latency & ndots:5 optimization
│   │   └── 10-inventory-concurrency-failure.md # Row-level locking & checkout deadlocks
│   ├── api/
│   │   └── api-reference.md                # REST API reference contracts with JSON payloads
│   ├── capstone/
│   │   ├── execution-roadmap.md            # Milestones, Gantt schedule & deliverables
│   │   ├── final-demo-script.md            # Step-by-step evaluator presentation script
│   │   └── syllabus-mapping.md             # DevOps & SRE competency matrix
│   └── final-audit.md                      # Comprehensive 30-item architectural & filesystem audit
│
├── finops/                                 # Cloud Financial Management (FinOps)
│   ├── cost-allocation-model.md            # Tagging taxonomy & Graviton cost optimization
│   └── cloud-cost-calculator.py            # Monthly AWS infrastructure cost estimator
│
├── frontend/                               # React + Vite + TypeScript Frontend SPA
│   ├── package.json                        # Frontend dependencies (React 18, Axios, Lucide)
│   ├── tsconfig.json                       # TypeScript compiler options
│   ├── vite.config.ts                      # Vite configuration & dev API reverse proxy
│   ├── index.html                          # Entry HTML with Outfit/Inter Google fonts & SEO
│   ├── nginx.conf                          # Production NGINX SPA routing configuration
│   ├── Dockerfile                          # Multi-stage Dockerfile (Node 20 -> NGINX 1.25)
│   └── src/
│       ├── main.tsx                        # React application bootstrap
│       ├── App.tsx                         # Client router & layout wrapper
│       ├── index.css                       # Glassmorphism styling tokens & design system
│       ├── types/
│       │   └── index.ts                    # TypeScript data interfaces
│       ├── services/
│       │   └── api.ts                      # Axios client with JWT interceptors
│       ├── context/
│       │   ├── AuthContext.tsx             # Authentication state & JWT handling
│       │   └── CartContext.tsx             # Shopping cart state & Redis sync
│       ├── components/
│       │   ├── Navbar.tsx                  # Header with brand, live cart badge, auth
│       │   ├── Footer.tsx                  # Architecture highlights & footer links
│       │   ├── ProductCard.tsx             # Product card with live stock badge & add button
│       │   └── ProtectedRoute.tsx          # Route guard for authenticated views
│       └── pages/
│           ├── HomePage.tsx                # Hero banner, category tabs, product catalog
│           ├── ProductDetailPage.tsx       # Live inventory stock check & quantity selector
│           ├── CartPage.tsx                # Item quantities, subtotal, clear cart
│           ├── CheckoutPage.tsx            # Idempotent order placement & shipping form
│           ├── OrdersPage.tsx              # Order history, status badges & cancellation
│           ├── NotificationsPage.tsx       # Real-time RabbitMQ notification feed
│           ├── LoginPage.tsx               # Login form with validation
│           └── RegisterPage.tsx            # User registration form
│
├── helm/                                   # Helm v3 Package Management
│   └── shopsphere/
│       ├── Chart.yaml                      # Chart metadata & versioning
│       ├── values.yaml                     # Default configuration values
│       ├── values-dev.yaml                 # Development environment overrides
│       ├── values-staging.yaml             # Pre-production environment overrides
│       ├── values-prod.yaml                # Production environment overrides with autoscaling
│       └── templates/
│           ├── _helpers.tpl                # Template helper functions & standard labels
│           ├── configmap.yaml              # Environment configuration template
│           ├── secrets.yaml                # Sensitive credentials template
│           ├── serviceaccount.yaml         # Per-service ServiceAccount definitions
│           ├── rbac.yaml                   # Fine-grained Role and RoleBinding rules
│           ├── networkpolicy.yaml          # Least-privilege ingress/egress network policies
│           ├── pdb.yaml                    # PodDisruptionBudget high availability rules
│           ├── hpa.yaml                    # HorizontalPodAutoscaler autoscaling rules
│           ├── services.yaml               # ClusterIP service definitions
│           ├── deployments.yaml            # Parameterized microservices deployments with probes
│           └── ingress.yaml                # Kubernetes ingress routing template
│
├── k8s/                                    # Raw Kubernetes Manifests & Kustomize
│   ├── base/
│   │   ├── configmap.yaml                  # Base environment variables
│   │   ├── secrets.yaml                    # Base secrets
│   │   ├── serviceaccounts.yaml            # Least-privilege service accounts
│   │   ├── rbac.yaml                       # Service RBAC Roles and RoleBindings
│   │   ├── networkpolicies.yaml            # Microservice network traffic isolation
│   │   ├── poddisruptionbudgets.yaml       # PDB high availability configurations
│   │   ├── hpa.yaml                        # Base Horizontal Pod Autoscaler definitions
│   │   ├── deployments.yaml                # Hardened deployments (SecurityContext + probes)
│   │   ├── ingress.yaml                    # Ingress rules routing API & frontend
│   │   └── kustomization.yaml              # Base Kustomize manifest
│   └── overlays/
│       ├── dev/
│       │   └── kustomization.yaml          # Dev namespace & single-replica patch
│       ├── staging/
│       │   ├── kustomization.yaml          # Staging namespace & 2-replica patch
│       │   └── hpa-staging.yaml            # Staging HPA thresholds
│       └── prod/
│           ├── kustomization.yaml          # Prod namespace & multi-replica patch
│           └── hpa.yaml                    # HorizontalPodAutoscalers (CPU utilization)
│
├── monitoring/                             # Observability & Metrics Stack
│   ├── prometheus/
│   │   ├── prometheus.yml                  # Scrape configuration for all 6 microservices
│   │   └── alert_rules.yml                 # SLO alert rules (5xx > 1%, p95 > 500ms, JVM)
│   ├── grafana/
│   │   ├── provisioning/
│   │   │   ├── datasources/datasources.yml # Auto-provisioned Prometheus & Loki sources
│   │   │   └── dashboards/dashboards.yml   # Dashboard provider setup
│   │   └── dashboards/
│   │       └── microservices-overview.json # Production dashboard (RPS, latency, 5xx, heap)
│   ├── loki/
│   │   └── loki-config.yml                 # Loki log ingestion configuration
│   ├── promtail/
│   │   └── promtail-config.yml             # Promtail container log scraper
│   └── otel/
│       └── otel-collector-config.yml       # OpenTelemetry Collector traces & metrics pipeline
│
├── scripts/                                # Automation & Operations Scripts
│   ├── build.sh                            # Full platform build (Maven + Vite)
│   ├── start.sh                            # Launch Docker Compose stack with healthcheck
│   ├── stop.sh                             # Graceful stack termination
│   ├── health-check.sh                     # Automated actuator health check across services
│   ├── backup-db.sh                        # Compressed pg_dump backup script
│   ├── restore-db.sh                       # PostgreSQL database restore script
│   ├── docker-build.sh                     # Build and tag all container images
│   ├── kind-setup.sh                       # Automated Kind Kubernetes cluster setup
│   └── deploy.sh                           # Kubernetes deployment script (Helm / Kustomize)
│
├── services/                               # 6 Spring Boot 3 Microservices (Java 21)
│   │
│   ├── auth-service/                       # Port 8081: Identity & Access Management
│   │   ├── pom.xml                         # Maven dependencies (Security, JWT, JPA, Flyway)
│   │   ├── Dockerfile                      # Multi-stage build (Temurin 21 JRE Alpine)
│   │   ├── src/main/java/com/shopsphere/auth/ # Controllers, DTOs, Security, Services
│   │   ├── src/main/resources/             # application.yml & Flyway V1 (users table)
│   │   └── src/test/                       # Unit & Testcontainers integration tests
│   │
│   ├── product-service/                    # Port 8082: Catalog & Categories
│   │   ├── pom.xml                         # Maven dependencies
│   │   ├── Dockerfile                      # Multi-stage build
│   │   ├── src/main/java/com/shopsphere/product/ # Controllers, DTOs, Services
│   │   ├── src/main/resources/             # application.yml & Flyway V1 (products, categories)
│   │   └── src/test/                       # ProductService unit tests
│   │
│   ├── cart-service/                       # Port 8083: Redis-Backed Cart
│   │   ├── pom.xml                         # Maven dependencies (Spring Data Redis)
│   │   ├── Dockerfile                      # Multi-stage build
│   │   ├── src/main/java/com/shopsphere/cart/ # Controller, Redis Cart Services
│   │   ├── src/main/resources/             # application.yml & Redis configuration
│   │   └── src/test/                       # CartService unit tests
│   │
│   ├── order-service/                      # Port 8084: Order Orchestration & Events
│   │   ├── pom.xml                         # Maven dependencies (RabbitMQ, WebClient, JPA)
│   │   ├── Dockerfile                      # Multi-stage build
│   │   ├── src/main/java/com/shopsphere/order/ # Idempotent checkout, RabbitMQ publisher
│   │   ├── src/main/resources/             # application.yml & Flyway V1 (orders, items)
│   │   └── src/test/                       # OrderService unit tests
│   │
│   ├── inventory-service/                  # Port 8085: High-Concurrency Stock
│   │   ├── pom.xml                         # Maven dependencies
│   │   ├── Dockerfile                      # Multi-stage build
│   │   ├── src/main/java/com/shopsphere/inventory/ # Pessimistic row locking repository
│   │   ├── src/main/resources/             # application.yml & Flyway V1 (inventory table)
│   │   └── src/test/                       # Concurrency race & Testcontainers tests
│   │
│   └── notification-service/               # Port 8086: Event Consumer & History
│       ├── pom.xml                         # Maven dependencies (RabbitMQ listener, JPA)
│       ├── Dockerfile                      # Multi-stage build
│       ├── src/main/java/com/shopsphere/notification/ # Consumer, DLQ setup, history API
│       ├── src/main/resources/             # application.yml & Flyway V1 (notifications table)
│       └── src/test/                       # Unit & event consumption tests
│
├── sre/                                    # Site Reliability Engineering (SRE)
│   ├── slo-sli-definitions.md              # 99.9% availability, p95 latency & burn rate alerts
│   ├── error-budget-policy.md              # Feature freeze triggers & post-mortem policies
│   ├── incident-response-runbook.md        # P1-P3 severity matrix, triage, and rollback
│   └── chaos-engineering.md                # Chaos Mesh pod kill & network partition tests
│
├── terraform/                              # Terraform Infrastructure as Code (AWS)
│   ├── modules/
│   │   ├── vpc/                            # 3 AZs, public/private subnets, NAT, ELB tags
│   │   ├── eks/                            # Managed Node Groups, OIDC provider
│   │   ├── rds/                            # Multi-AZ PostgreSQL RDS instance
│   │   ├── elasticache/                    # Redis replication group with automatic failover
│   │   └── iam/                            # IRSA for ALB controller, EBS CSI, Secrets
│   └── environments/
│       ├── dev/                            # Single NAT, t3 instances, non-prod cost focus
│       ├── staging/                        # Multi-AZ NAT, t3.large, multi-node RDS/Redis
│       └── prod/                           # Multi-AZ NAT, Graviton m6g/r6g, 3-node Redis
│
├── tests/load/                             # Performance & Concurrency Load Tests (k6)
│   ├── k6-browse-load-test.js              # Catalog browsing & category filtering test
│   ├── k6-checkout-stress-test.js          # Full registration, cart & idempotent checkout test
│   ├── k6-concurrency-lock-test.js         # Flash-sale concurrency race test (50 concurrent VUs)
│   └── k6-autoscaling-demo.js              # 15-minute load profile triggering HPA autoscaling
│
├── .dockerignore                           # Docker build context exclusions
├── .env.example                            # Master environment variable template
├── .gitignore                              # Git version control exclusions
├── docker-compose.yml                      # Full local platform Compose file (services + proxy)
├── docker-compose.dev.yml                  # Backing services only (PostgreSQL, Redis, RabbitMQ)
├── docker-compose.observability.yml        # Observability stack (Prometheus, Grafana, Loki)
├── LICENSE                                 # MIT License
├── README.md                               # Master project documentation
└── structure.md                            # Comprehensive codebase structure map (this file)
```

---

## 2. Port Allocation & Gateway Map

| Component | Container Port | Host Port | Routing Path (via Ingress / NGINX) | Protocol |
| :--- | :--- | :--- | :--- | :--- |
| **NGINX / Ingress Gateway**| `80` | `80` | `/` | HTTP |
| **Frontend SPA** | `80` | `3000` | `/` | HTTP |
| **auth-service** | `8081` | `8081` | `/api/auth/*` | HTTP |
| **product-service** | `8082` | `8082` | `/api/products/*`, `/api/categories/*` | HTTP |
| **cart-service** | `8083` | `8083` | `/api/cart/*` | HTTP |
| **order-service** | `8084` | `8084` | `/api/orders/*` | HTTP |
| **inventory-service** | `8085` | `8085` | `/api/inventory/*` | HTTP |
| **notification-service** | `8086` | `8086` | `/api/notifications/*` | HTTP |
| **PostgreSQL Database** | `5432` | `5432` | Internal | TCP |
| **Redis Cache** | `6379` | `6379` | Internal | TCP |
| **RabbitMQ AMQP** | `5672` | `5672` | Internal | AMQP |
| **RabbitMQ Management** | `15672` | `15672`| `http://localhost:15672` | HTTP |
| **Prometheus Server** | `9090` | `9090` | `http://localhost:9090` | HTTP |
| **Grafana Dashboard** | `3000` | `3001` | `http://localhost:3001` | HTTP |
| **Loki Log Collector** | `3100` | `3100` | Internal | HTTP |
| **OpenTelemetry Collector**| `4317` / `4318` | `4317` / `4318` | Internal | gRPC / HTTP |

---

## 3. Database Schema Allocation (PostgreSQL)

| Database Name | Owning Microservice | Tables Managed via Flyway |
| :--- | :--- | :--- |
| `auth_db` | `auth-service` | `users`, `flyway_schema_history` |
| `product_db` | `product-service` | `categories`, `products`, `flyway_schema_history` |
| `order_db` | `order-service` | `orders`, `order_items`, `flyway_schema_history` |
| `inventory_db` | `inventory-service` | `inventory`, `flyway_schema_history` |
| `notification_db` | `notification-service` | `notifications`, `flyway_schema_history` |

*(Shopping cart sessions are decoupled into Redis to maximize throughput and minimize relational database load).*
