variable "aws_region" {
  type        = string
  description = "Target AWS region"
  default     = "us-east-1"
}

variable "db_password" {
  type        = string
  description = "Database master password"
  sensitive   = true
}
