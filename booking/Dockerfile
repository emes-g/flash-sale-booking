# 1. Base Image: 가볍고 보안성이 높은 Alpine Linux 기반의 OpenJDK 17 사용
FROM eclipse-temurin:17-jdk-alpine

# 2. 컨테이너 내에서 작업할 디렉토리 지정
WORKDIR /app

# 3. 호스트(로컬)의 JAR 파일을 컨테이너 내부(app.jar)로 복사
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. 컨테이너의 8080 포트를 외부에 개방
EXPOSE 8080

# 5. 애플리케이션 실행 명령어 (프로파일 설정이나 환경변수는 나중에 docker-compose에서 주입)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]