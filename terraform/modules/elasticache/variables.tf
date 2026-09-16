variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID where Redis will be deployed"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for ElastiCache subnet group"
}

variable "allowed_security_group_ids" {
  type        = list(string)
  description = "List of security group IDs permitted to connect to Redis"
  default     = []
}

variable "node_type" {
  type        = string
  description = "ElastiCache node instance type"
  default     = "cache.t4g.small"
}

variable "num_cache_nodes" {
  type        = number
  description = "Number of cache nodes (1 for dev, 2+ for multi-AZ failover)"
  default     = 1
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
