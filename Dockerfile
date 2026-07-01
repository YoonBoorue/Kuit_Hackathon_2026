# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Gradle wrapper / build files first for better layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle* settings.gradle* ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon \
    && JAR_FILE=$(find build/libs -name "*.jar" ! -name "*plain.jar" | head -n 1) \
    && cp "$JAR_FILE" app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=builder /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
