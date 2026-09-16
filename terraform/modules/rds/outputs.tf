output "db_instance_endpoint" {
  description = "Connection endpoint for PostgreSQL database"
  value       = aws_db_instance.main.endpoint
}

output "db_instance_address" {
  description = "Hostname of the RDS instance"
  value       = aws_db_instance.main.address
}

output "db_instance_port" {
  description = "Port the database listens on"
  value       = aws_db_instance.main.port
}

output "db_security_group_id" {
  description = "ID of the database security group"
  value       = aws_security_group.rds.id
}
