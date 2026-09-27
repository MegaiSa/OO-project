# One Dockerfile for both services: docker build --build-arg SERVICE=chat-api .
FROM gradle:8.14-jdk21 AS build
ARG SERVICE
WORKDIR /app
COPY . .
RUN gradle :${SERVICE}:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY --from=build /app/${SERVICE}/build/libs/${SERVICE}-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
