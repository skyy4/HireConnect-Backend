# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY common ./common
COPY profile-service ./profile-service
RUN mvn -B package -pl profile-service --also-make -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/profile-service/target/profile-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
