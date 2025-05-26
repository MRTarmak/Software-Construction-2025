FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts ./
COPY src ./src
RUN apt-get update && apt-get install -y curl
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]