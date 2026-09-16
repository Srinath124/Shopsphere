variable "cluster_name" {
  type        = string
  description = "Name of the EKS cluster"
}

variable "kubernetes_version" {
  type        = string
  description = "Desired Kubernetes control plane version"
  default     = "1.29"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet IDs for EKS cluster load balancers"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for EKS worker nodes"
}

variable "instance_types" {
  type        = list(string)
  description = "EC2 instance types for EKS worker node group"
  default     = ["t3.medium"]
}

variable "capacity_type" {
  type        = string
  description = "Capacity type for worker nodes (ON_DEMAND or SPOT)"
  default     = "ON_DEMAND"
}

variable "desired_capacity" {
  type        = number
  description = "Desired number of worker nodes"
  default     = 3
}

variable "min_capacity" {
  type        = number
  description = "Minimum number of worker nodes"
  default     = 2
}

variable "max_capacity" {
  type        = number
  description = "Maximum number of worker nodes"
  default     = 6
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
