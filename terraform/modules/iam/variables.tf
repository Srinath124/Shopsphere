variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "oidc_provider_arn" {
  type        = string
  description = "ARN of the EKS OIDC provider"
}

variable "oidc_provider_url" {
  type        = string
  description = "URL of the EKS OIDC provider"
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
