# Stage 1: Build the application using Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B 2>/dev/null || true

# Copy application source code and package executable artifact
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal, secure JRE 21 runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-privileged system user for security
RUN addgroup -S campusflow && adduser -S campusflow -G campusflow
USER campusflow

# Copy artifact from builder stage
COPY --from=builder /app/target/campusflow-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=${PORT}", "-jar", "app.jar"]
