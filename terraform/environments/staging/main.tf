terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }

  # Remote state backend (uncomment when OPTIONAL AWS deployment is used)
  # See docs/deployment/terraform-remote-state.md for setup instructions.
  # backend "s3" {
  #   bucket         = "shopsphere-terraform-state"
  #   key            = "staging/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "shopsphere-terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "ShopSphere"
      Environment = "staging"
      ManagedBy   = "Terraform"
    }
  }
}

locals {
  environment  = "staging"
  cluster_name = "shopsphere-eks-staging"
}

module "vpc" {
  source = "../../modules/vpc"

  environment          = local.environment
  cluster_name         = local.cluster_name
  vpc_cidr             = "10.20.0.0/16"
  availability_zones   = ["us-east-1a", "us-east-1b", "us-east-1c"]
  public_subnet_cidrs  = ["10.20.1.0/24", "10.20.2.0/24", "10.20.3.0/24"]
  private_subnet_cidrs = ["10.20.11.0/24", "10.20.12.0/24", "10.20.13.0/24"]
  single_nat_gateway   = true
}

module "eks" {
  source = "../../modules/eks"

  cluster_name       = local.cluster_name
  kubernetes_version = "1.29"
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  instance_types     = ["t3.large"]
  capacity_type      = "ON_DEMAND"
  desired_capacity   = 3
  min_capacity       = 2
  max_capacity       = 8
}

module "rds" {
  source = "../../modules/rds"

  environment             = local.environment
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  instance_class          = "db.t4g.medium"
  allocated_storage       = 50
  multi_az                = true
  admin_username          = "shopsphere"
  admin_password          = var.db_password
  backup_retention_period = 7
}

module "elasticache" {
  source = "../../modules/elasticache"

  environment        = local.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  node_type          = "cache.t4g.medium"
  num_cache_nodes    = 2
}

module "iam" {
  source = "../../modules/iam"

  environment       = local.environment
  oidc_provider_arn = module.eks.oidc_provider_arn
  oidc_provider_url = module.eks.oidc_provider_url
}
