FROM eclipse-temurin:21-jdk

WORKDIR /app

# 설정 파일 복사 (서브모듈 전체 또는 필요한 파일만)
COPY 2025-1-SBConfig /app/2025-1-SBConfig

# 빌드된 jar 복사
COPY build/libs/sbstudy-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]