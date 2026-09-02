# ------------------------------
# Build stage
# ------------------------------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Copy API spec (needed for OpenAPI codegen)
COPY api ./api

# Copy proto submodule (needed for gRPC codegen)
COPY saasy-proto ./saasy-proto

# Download dependencies (layer caching)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon

# ------------------------------
# Runtime stage
# ------------------------------
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar ./app.jar

# Expose ports (REST and gRPC)
EXPOSE 8082 9092

# Run the application
CMD ["java", "-jar", "app.jar"]
