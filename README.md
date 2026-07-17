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
🗂 코드 구조 (역할별)                                                                                                                                                                                                                                                                                                                                                                                             
com.musinsa.course                                                                                                                                                                                      
├── Application.java              앱 시작점                                                                                                                                                             
├── api/                          ← HTTP 계층 (Controller)                                                                                                                                              
│   ├── HealthController          /health (준비 완료 게이트)                                                                                                                                            
│   ├── Students/Courses/         목록 조회 (페이징)                                                                                                                                                    
│   │   ProfessorsController                                                                                                                                                                            
│   ├── EnrollmentsController     수강신청/취소 ★                                                                                                                                                       
│   ├── TimetableController       내 시간표                                                                                                                                                             
│   └── response/                 공통 응답 DTO                                                                                                                                                         
└── data/                         ← 핵심 로직 계층                                                                                                                                                      
├── InMemoryStore.java (412줄) 저장소 + 동시성 + 비즈니스 규칙 ★★★                                                                                                                                  
├── SeedData.java             데이터 모델(record)                                                                                                                                                   
└── SeedDataGenerator.java    현실적 데이터 생성

🎓 이 프로젝트에 담긴 학습 포인트 (기술별)

1. 동시성 제어 — ReentrantLock, 락 순서 고정으로 데드락 회피 (courseId → studentId), 임계구역 설계 → 가장 중요, 핵심
2. 인메모리 동시성 자료구조 — ConcurrentHashMap, AtomicLong, newKeySet, volatile
3. Spring Boot REST API — Controller, DTO, 에러 코드 체계, HTTP 상태 매핑
4. 준비 상태 게이트 — 데이터 생성 완료 전 503, 완료 후 200 (/health)
5. Java 최신 문법 — record, Stream, 팩토리 메서드 패턴
6. 페이지네이션 — limit/offset 유효성 검사
7. 비즈니스 규칙 — 18학점 제한, 시간표 파싱 & 충돌 판정