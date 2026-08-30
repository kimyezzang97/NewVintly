# 1. 빌드 환경이 아닌 '실행' 환경이므로 가벼운 JRE(Java Runtime Environment) 기반 이미지를 사용합니다.
FROM eclipse-temurin:17-jre-alpine

# 2. 컨테이너 내부 작업 디렉토리 생성
WORKDIR /app

# 3. Jenkins가 빌드한 jar 파일을 컨테이너 내부로 복사
# 빌드 로그에서 확인했듯이 build/libs/ 폴더에 생성된 jar를 가져옵니다.
COPY build/libs/*-SNAPSHOT.jar app.jar

# 4. 타임존 설정 (한국 시간 기준)
ENV TZ=Asia/Seoul

# 5. 실행 명령어 (메모리 2GB 설계를 고려하여 제한 설정 추가)
# -Xmx1536m: 최대 힙 메모리를 1.5GB로 제한 (OS/기타 메모리 여유분 확보)
ENTRYPOINT ["java", "-jar", "-Xmx1536m", "-Dspring.profiles.active=dev", "app.jar"]