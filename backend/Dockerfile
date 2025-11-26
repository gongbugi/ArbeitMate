# [수정] openjdk 대신 eclipse-temurin 사용
FROM eclipse-temurin:21-jre

# 워크플로우에서 빌드된 jar 파일을 이미지 내부로 복사
COPY build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행 명령어 (배포용 이미지는 docker 프로필로 빌드)
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]