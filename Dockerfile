FROM maven:3.9.4-eclipse-temurin-17 as build

COPY src src
COPY pom.xml pom.xml

RUN mvn clean package

FROM bellsoft/liberica-openjdk-debian:17

RUN adduser --system spring-boot && addgroup --system spring-boot
USER spring-boot

WORKDIR /app

COPY --from=build target/card-management-0.0.1-SNAPSHOT.jar ./application.jar