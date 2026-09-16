resource "aws_db_subnet_group" "main" {
  name        = "${var.environment}-shopsphere-rds-subnet-group"
  subnet_ids  = var.private_subnet_ids
  description = "Subnet group for ShopSphere PostgreSQL instances"

  tags = var.tags
}

resource "aws_security_group" "rds" {
  name        = "${var.environment}-shopsphere-rds-sg"
  description = "Controls traffic to ShopSphere PostgreSQL database"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow PostgreSQL access from worker nodes"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = var.allowed_security_group_ids
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = var.tags
}

resource "aws_db_parameter_group" "main" {
  name   = "${var.environment}-shopsphere-pg16-params"
  family = "postgres16"

  parameter {
    name  = "rds.force_ssl"
    value = "1"
  }

  tags = var.tags
}

resource "aws_db_instance" "main" {
  identifier                  = "${var.environment}-shopsphere-postgres"
  engine                      = "postgres"
  engine_version              = "16.2"
  instance_class              = var.instance_class
  allocated_storage           = var.allocated_storage
  max_allocated_storage       = var.max_allocated_storage
  storage_type                = "gp3"
  storage_encrypted           = true
  multi_az                    = var.multi_az
  db_name                     = var.database_name
  username                    = var.admin_username
  password                    = var.admin_password
  db_subnet_group_name        = aws_db_subnet_group.main.name
  vpc_security_group_ids      = [aws_security_group.rds.id]
  parameter_group_name        = aws_db_parameter_group.main.name
  auto_minor_version_upgrade  = true
  allow_major_version_upgrade = false
  skip_final_snapshot         = var.environment != "prod"
  final_snapshot_identifier   = "${var.environment}-shopsphere-postgres-final-snapshot"
  deletion_protection         = var.environment == "prod"
  backup_retention_period     = var.backup_retention_period

  tags = var.tags
}
