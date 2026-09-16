resource "aws_elasticache_subnet_group" "main" {
  name        = "${var.environment}-shopsphere-redis-subnet-group"
  subnet_ids  = var.private_subnet_ids
  description = "Subnet group for ShopSphere Redis cluster"

  tags = var.tags
}

resource "aws_security_group" "redis" {
  name        = "${var.environment}-shopsphere-redis-sg"
  description = "Security group controlling Redis cache ingress"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow Redis access from worker nodes"
    from_port       = 6379
    to_port         = 6379
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

resource "aws_elasticache_replication_group" "main" {
  replication_group_id       = "${var.environment}-shopsphere-cart-cache"
  description                = "Redis cluster for distributed cart session management"
  node_type                  = var.node_type
  num_cache_clusters         = var.num_cache_nodes
  port                       = 6379
  parameter_group_name       = "default.redis7"
  subnet_group_name          = aws_elasticache_subnet_group.main.name
  security_group_ids         = [aws_security_group.redis.id]
  at_rest_encryption_enabled = true
  transit_encryption_enabled = false
  automatic_failover_enabled = var.num_cache_nodes > 1

  tags = var.tags
}
