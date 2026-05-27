# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy wrapper files and cached dependency metadata
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Explicitly copy only the Java backend src directory
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ==========================================
# Stage 2: Hardened, Light Runtime Profile
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Safely transport the compiled artifact from the builder phase
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot port allocation
EXPOSE 8080

# Production Health Monitoring Interceptor
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Production JVM Memory & Container Aware Directives
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]