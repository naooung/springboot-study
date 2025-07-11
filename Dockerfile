FROM eclipse-temurin:21-jdk

WORKDIR /app

# 빌드된 jar 복사
COPY build/libs/sbstudy-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]