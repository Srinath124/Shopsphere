terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }

  backend "s3" {
    bucket         = "shopsphere-terraform-state-prod"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "shopsphere-terraform-locks-prod"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "ShopSphere"
      Environment = "prod"
      ManagedBy   = "Terraform"
      Criticality = "High"
    }
  }
}

locals {
  environment  = "prod"
  cluster_name = "shopsphere-eks-prod"
}

module "vpc" {
  source = "../../modules/vpc"

  environment          = local.environment
  cluster_name         = local.cluster_name
  vpc_cidr             = "10.0.0.0/16"
  availability_zones   = ["us-east-1a", "us-east-1b", "us-east-1c"]
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  private_subnet_cidrs = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
  single_nat_gateway   = false # Multi-AZ NAT Gateways for HA
}

module "eks" {
  source = "../../modules/eks"

  cluster_name       = local.cluster_name
  kubernetes_version = "1.29"
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  instance_types     = ["m6i.large", "m5.large"]
  capacity_type      = "ON_DEMAND"
  desired_capacity   = 4
  min_capacity       = 3
  max_capacity       = 10
}

module "rds" {
  source = "../../modules/rds"

  environment             = local.environment
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  instance_class          = "db.r6g.large"
  allocated_storage       = 100
  max_allocated_storage   = 500
  multi_az                = true # Production Multi-AZ Failover
  admin_username          = "shopsphere"
  admin_password          = var.db_password
  backup_retention_period = 30
}

module "elasticache" {
  source = "../../modules/elasticache"

  environment        = local.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  node_type          = "cache.m6g.large"
  num_cache_nodes    = 3 # Multi-AZ Redis Replication
}

module "iam" {
  source = "../../modules/iam"

  environment       = local.environment
  oidc_provider_arn = module.eks.oidc_provider_arn
  oidc_provider_url = module.eks.oidc_provider_url
}
