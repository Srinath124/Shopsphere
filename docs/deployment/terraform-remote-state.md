# Terraform Remote State — Setup Guide

> [!IMPORTANT]
> **This document describes OPTIONAL AWS infrastructure.**
> Local development with Docker Compose, Kind, and Kubernetes requires **zero AWS credentials**.
> Only proceed with this guide when deploying to a real AWS environment.

## Overview

Terraform state must be stored remotely when multiple team members or CI/CD pipelines
run Terraform against the same infrastructure. ShopSphere uses:

- **S3** for state storage (versioned, encrypted)
- **DynamoDB** for state locking (prevents concurrent `terraform apply` runs)

## Architecture

```
terraform apply (dev machine / CI)
        │
        ▼
┌─────────────────────────────┐
│  S3 Bucket (State Storage)  │
│  shopsphere-terraform-state │
│  Key: dev/terraform.tfstate │
│  Key: staging/...           │
│  Key: prod/...              │
└─────────────────────────────┘
        │
        ▼
┌─────────────────────────────────┐
│  DynamoDB Table (State Lock)    │
│  shopsphere-terraform-locks     │
│  Partition Key: LockID (String) │
└─────────────────────────────────┘
```

## Bootstrap Steps (One-time, run manually)

### Step 1 — Create S3 Bucket

```bash
export AWS_REGION=us-east-1
export STATE_BUCKET=shopsphere-terraform-state-$(aws sts get-caller-identity --query Account --output text)

aws s3api create-bucket \
  --bucket "$STATE_BUCKET" \
  --region "$AWS_REGION" \
  --create-bucket-configuration LocationConstraint="$AWS_REGION"

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket "$STATE_BUCKET" \
  --versioning-configuration Status=Enabled

# Enable server-side encryption
aws s3api put-bucket-encryption \
  --bucket "$STATE_BUCKET" \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# Block all public access
aws s3api put-public-access-block \
  --bucket "$STATE_BUCKET" \
  --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

echo "State bucket: $STATE_BUCKET"
```

### Step 2 — Create DynamoDB Lock Table

```bash
aws dynamodb create-table \
  --table-name shopsphere-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "$AWS_REGION"
```

### Step 3 — Enable Backend in Each Environment

Uncomment the backend block in each environment's `main.tf`:

```hcl
# terraform/environments/dev/main.tf
backend "s3" {
  bucket         = "shopsphere-terraform-state-123456789012"
  key            = "dev/terraform.tfstate"
  region         = "us-east-1"
  dynamodb_table = "shopsphere-terraform-locks"
  encrypt        = true
}
```

Repeat for `staging/` and `prod/`.

### Step 4 — Migrate Local State to Remote

```bash
cd terraform/environments/dev
terraform init -migrate-state
# Confirm "yes" when prompted
```

## GitHub Actions Integration

For CI/CD, set these repository secrets:

| Secret | Value |
|--------|-------|
| `AWS_ACCESS_KEY_ID` | IAM user/role with Terraform permissions |
| `AWS_SECRET_ACCESS_KEY` | Corresponding secret key |
| `TF_VAR_DB_PASSWORD` | Database master password (sensitive) |

The GitHub Actions CD workflow uses these automatically for `terraform plan` and `apply`.

## State File Security

- State files contain **sensitive data** (DB passwords, ARNs, certificates)
- Always use `encrypt = true` in the S3 backend
- Restrict S3 bucket access to Terraform-specific IAM roles only
- Enable S3 versioning to recover from accidental state corruption

## Rollback Procedure

```bash
# List previous state versions
aws s3api list-object-versions \
  --bucket "$STATE_BUCKET" \
  --prefix "prod/terraform.tfstate"

# Restore a specific version
aws s3api get-object \
  --bucket "$STATE_BUCKET" \
  --key "prod/terraform.tfstate" \
  --version-id "VERSION_ID_HERE" \
  terraform.tfstate.backup
```
