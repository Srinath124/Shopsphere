variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID where database will be deployed"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for database subnet group"
}

variable "allowed_security_group_ids" {
  type        = list(string)
  description = "List of security group IDs permitted to connect to PostgreSQL (e.g. EKS nodes)"
  default     = []
}

variable "instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t4g.medium"
}

variable "allocated_storage" {
  type        = number
  description = "Initial allocated storage in GB"
  default     = 20
}

variable "max_allocated_storage" {
  type        = number
  description = "Storage autoscaling limit in GB"
  default     = 100
}

variable "multi_az" {
  type        = bool
  description = "Enable Multi-AZ deployment for failover"
  default     = false
}

variable "database_name" {
  type        = string
  description = "Initial database name"
  default     = "auth_db"
}

variable "admin_username" {
  type        = string
  description = "Database administrator username"
  default     = "shopsphere"
}

variable "admin_password" {
  type        = string
  description = "Database administrator password"
  sensitive   = true
}

variable "backup_retention_period" {
  type        = number
  description = "Number of days to retain automated database backups"
  default     = 7
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
