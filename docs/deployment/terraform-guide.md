# Terraform & AWS Cloud Provisioning Guide
## ShopSphere / Go N Buy Platform

### 1. Prerequisites
- AWS CLI configured with administrator or platform engineer credentials (`aws configure`)
- Terraform v1.5.0+

---

### 2. Provisioning the Dev Environment

```bash
cd terraform/environments/dev

# 1. Initialize Terraform providers and modules
terraform init

# 2. Plan infrastructure execution
terraform plan -out=tfplan

# 3. Apply infrastructure
terraform apply tfplan
```

---

### 3. Provisioning Production Infrastructure

Production infrastructure deploys multi-AZ NAT Gateways, multi-AZ PostgreSQL RDS (`db.r6g.large`), 3-node Redis ElastiCache with failover, and EKS Managed Node Groups scaling up to 10 instances:

```bash
cd terraform/environments/prod

terraform init
terraform plan -var="db_password=YOUR_VAULT_SECRET_PASS" -out=tfplan
terraform apply tfplan
```

#### Configuring kubectl for EKS:
```bash
aws eks update-kubeconfig --region us-east-1 --name shopsphere-eks-prod
kubectl get nodes
```

---

### 4. Teardown
To avoid unnecessary cloud charges when finished testing:
```bash
terraform destroy -auto-approve
```
