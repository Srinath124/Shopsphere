# ============================================================
# auth-service Dockerfile
# Multi-stage build — Java 21 + Maven
# Runs as non-root user
# ============================================================

# Stage 1: Build
FROM maven:3.9.7-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for health check
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/auth-service-*.jar app.jar

# Own the workdir
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
