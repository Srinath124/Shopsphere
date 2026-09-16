variable "environment" {
  type        = string
  description = "Target deployment environment (dev, staging, prod)"
}

variable "cluster_name" {
  type        = string
  description = "EKS cluster name to tag subnets for ELB discovery"
}

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR block"
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  type        = list(string)
  description = "List of availability zones"
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "List of public subnet CIDRs"
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "List of private subnet CIDRs"
  default     = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
}

variable "single_nat_gateway" {
  type        = bool
  description = "Set true to provision a single NAT gateway to minimize dev costs"
  default     = true
}

variable "tags" {
  type        = map(string)
  description = "Common resource tags"
  default     = {}
}
