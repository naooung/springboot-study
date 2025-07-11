FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app
COPY 2025-1-SBConfig/application-local.properties 2025-1-SBConfig/application-local.properties

COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]