# Java 21 실행 환경 (JDK)
FROM openjdk:21-jdk-slim

# 워크플로우에서 빌드된 jar 파일을 이미지 내부로 복사
# (Gradle 빌드 결과물은 보통 build/libs/ 안에 생성됩니다)
COPY build/libs/*.jar app.jar

# 포트 설정 (Spring Boot 기본 포트)
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]