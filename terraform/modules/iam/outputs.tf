output "load_balancer_controller_role_arn" {
  description = "IAM Role ARN for AWS Load Balancer Controller"
  value       = aws_iam_role.load_balancer_controller.arn
}

output "ebs_csi_driver_role_arn" {
  description = "IAM Role ARN for EBS CSI Driver"
  value       = aws_iam_role.ebs_csi_driver.arn
}

output "external_secrets_role_arn" {
  description = "IAM Role ARN for External Secrets Operator"
  value       = aws_iam_role.external_secrets.arn
}
