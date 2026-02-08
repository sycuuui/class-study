# 실행 방법

필수 환경
- Java 21
- Gradle Wrapper 사용

서버 실행 (기본 포트 8080)
```bash
./gradlew bootRun
```

서버 준비 상태 확인
- 준비 전: HTTP 503 + error.code 700
- 준비 후: HTTP 200
```bash
curl -i http://localhost:8080/health
```

주요 API 간단 테스트
```bash
curl -i "http://localhost:8080/students?limit=1&offset=0"
```
```bash
curl -i -X POST "http://localhost:8080/enrollments" \
  -H "Content-Type: application/json" \
  -d '{"studentId":1001,"courseId":1}'
```

테스트 실행
```bash
./gradlew test
```
