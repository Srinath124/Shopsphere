variable "aws_region" {
  type        = string
  description = "Target AWS region"
  default     = "us-east-1"
}

variable "db_password" {
  type        = string
  description = "Production Database Master Password"
  sensitive   = true
}
