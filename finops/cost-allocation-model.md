# Cloud Financial Management (FinOps) & Cost Allocation Model
## ShopSphere / Go N Buy Platform

### 1. Cost Allocation Tagging Taxonomy

Every AWS and Kubernetes resource MUST be tagged with the following mandatory keys:

| Tag Key | Description | Example Values |
| :--- | :--- | :--- |
| `Environment` | Stage classification | `dev`, `staging`, `prod` |
| `Service` | Owning microservice | `auth-service`, `order-service`, `shared-infra` |
| `CostCenter` | Departmental budget identifier | `CC-ENG-ECOMM-401` |
| `Owner` | Responsible team/engineer | `team-checkout`, `devops-sre` |

---

### 2. Architectural Cost Optimization Levers

1. **AWS Graviton3 (ARM64) Migration**:
   - RDS instances run on `db.t4g` / `db.r6g` (Graviton), yielding **20% better price-performance** compared to x86 equivalents.
   - EKS worker node spot pools utilize `c6g.large` / `m6g.large`.

2. **Automated Non-Production Scheduling**:
   - Dev and staging Kubernetes namespaces automatically scale to 0 replicas between 8:00 PM and 7:00 AM weekdays and all weekend, eliminating ~65% of dev compute spend.

3. **NAT Gateway Consolidation**:
   - Dev environment provisions a single NAT gateway instead of 3 multi-AZ gateways, saving ~$64/month in idle gateway charges.

4. **Karpenter Autoscaling**:
   - Replaces traditional Cluster Autoscaler to rapidly provision right-sized Spot instances for burst checkout traffic.
