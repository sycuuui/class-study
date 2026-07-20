# 🏭 실무 적용 진단 (Production Readiness Assessment)

> 목표: 현재 인메모리 수강신청 시스템을 **실무에 사용할 수 있는 수준**으로 끌어올리기 위한 현황 진단.
> 한 문장 진단: **동시성 정확성이라는 가장 어려운 심장은 잘 뛰지만, 실무 서비스가 되려면 몸통(영속성·확장성·계층·검증·관측·보안·테스트)이 거의 다 빠져 있다.**

---

## ✅ Part A. 실무에 그대로 가져갈 수 있는 강점

| # | 강점 | 실무 가치 |
|---|---|---|
| 1 | **동시성 정확성** — 락 순서 고정(courseId→studentId), 임계구역 원자성, try/finally | 정원 초과 버그를 근본 차단. 그대로 통용 |
| 2 | **계층 분리** (api ↔ data) | 스프링 없이 로직 테스트 가능 |
| 3 | **준비 게이트** (`/health` isReady) | K8s readiness probe로 연결 |
| 4 | **에러코드 taxonomy** (500~700 + HTTP 매핑) | 체계는 훌륭, 표현만 개선 |
| 5 | **불변 DTO(record) + 내부/외부 모델 분리** | API 안정성의 정석 |
| 6 | **결정론적 시드 + CountDownLatch 동시성 테스트** | 재현 가능한 동시성 검증 |
| 7 | **페이지네이션 + 입력 검증** | 목록 API 기본기 |

---

## 🔧 Part B. 보완 항목 (우선순위별)

### 🔴 P0 — 이게 없으면 실무 서비스가 아님

| 영역 | 현재 상태 | 왜 문제 | 실무 해법 |
|---|---|---|---|
| **영속성(DB)** | 전부 인메모리, 재시작 시 전 데이터 소멸 | 서버 죽으면 수강신청 전부 소멸 | RDB(PostgreSQL/MySQL) + JPA, 트랜잭션 |
| **수평 확장 불가** | 락이 프로세스 내부(ReentrantLock) | 서버 2대면 각자 다른 락 → 정원 초과 재발 | DB 락(비관적 SELECT FOR UPDATE / 낙관적 @Version), 또는 Redis 분산락 |
| **인증/인가 전무** | body의 studentId를 그냥 신뢰 | 아무나 남의 학번으로 신청/취소 | Spring Security + JWT/세션, 본인 검증 |

> P0 핵심: "인메모리 + 프로세스 락" 전제 자체가 실무에선 성립 안 함. 최대 재설계 포인트.

### 🟠 P1 — 유지보수·안정성

| 영역 | 현재 상태 | 실무 해법 |
|---|---|---|
| **서비스 계층 부재** | 비즈니스 로직이 InMemoryStore(데이터 계층)에 뭉침 | Controller → Service → Repository 3계층 |
| **예외 처리 산발** | 컨트롤러마다 수동 if, 에러코드 매직넘버 10곳 | @RestControllerAdvice 전역 핸들러 + 커스텀 예외 + ErrorCode enum |
| **입력 검증 수동** | if(limit<1...) 손코딩 | Bean Validation(@Valid, @Min, @Max, @NotNull) |
| **테스트 빈약** | 단위 2개뿐(컨트롤러/통합/엣지 0) | @WebMvcTest, @SpringBootTest, 404·검증·취소·시간표 커버 |
| **성능 O(N) 스캔** | enroll마다 전체 enrollments 순회 + schedule 매번 재파싱 | 학생별 인덱스(Map<studentId, Set>), Schedule 사전 계산 |
| **관측성 전무** | 로그는 시드 타이밍뿐 | Actuator, 구조적 로깅, 요청 로깅, Micrometer 메트릭 |

### 🟡 P2 — 품질·완성도

| 영역 | 현재 상태 | 실무 해법 |
|---|---|---|
| **API 문서 자동화** | 수동 docs/API.md (불일치 위험) | springdoc-openapi(Swagger UI) |
| **설정 외부화** | 18학점·정원범위 하드코딩 | application.yml + @ConfigurationProperties, 프로파일 |
| **죽은/오해 필드** | Student.enrolledCredits 갱신 안 됨 | 제거 또는 실제 갱신 |
| **DELETE에 body** | DELETE /enrollments + JSON body(비표준) | DELETE /enrollments/{id} 경로 파라미터 |
| **빌드/배포** | Dockerfile·CI 없음, Java 버전 불일치(build=17/README=21) | Docker, GitHub Actions CI, 버전 통일 |
| **학기 개념 없음** | 단일 학기 가정 | semester 엔티티, 수강신청 기간 제어 |

---

## 📊 성숙도 레이더

```
동시성 정확성   ████████░░  8/10  ← 자랑거리
설계/계층       ████░░░░░░  4/10
영속성/확장성   █░░░░░░░░░  1/10  ← 최대 약점
보안            ░░░░░░░░░░  0/10
검증/예외처리   ███░░░░░░░  3/10
테스트          ██░░░░░░░░  2/10
관측성/운영     █░░░░░░░░░  1/10
API 완성도      ████░░░░░░  4/10
```

---

## 🗺 확정된 작업 범위 (2026-07 결정)

- **목표 수준: 학습용 리팩토링** — DB 도입 없이 **인메모리 유지**, 그 위에 실무 아키텍처를 입힘.
- **확장 전제: 단일 서버** — 현재 `ReentrantLock` 동시성 코어를 그대로 유지 (분산락/DB락 불필요).
- 따라서 P0(DB·수평확장·인증)는 이번 범위에서 제외, **P1 중심**으로 진행.

### 리팩토링 로드맵 (단일 서버 · 인메모리)

| 단계 | 내용 | 배우는 패턴 |
|---|---|---|
| **R1** | ErrorCode enum + 커스텀 예외 계층 + @RestControllerAdvice 전역 예외 처리 | 에러 표준화, 관심사 분리 |
| **R2** | 3계층 분리: Store → Repository + Service(비즈니스·락) + Controller(얇게) | 레이어드 아키텍처 |
| **R3** | Bean Validation(@Valid, @Min/@Max)로 수동 검증 제거 | 선언적 검증 |
| **R4** | 성능: 학생별 신청 인덱스, Schedule 사전 계산 | 자료구조 최적화 O(N)→O(k) |
| **R5** | 테스트 보강: @WebMvcTest 컨트롤러 + 서비스 단위 + 엣지케이스 | 테스트 피라미드 |
| **R6** | 마무리: 설정 외부화(application.yml), OpenAPI(Swagger), Actuator | 운영 편의 |

진행 방식: 각 단계 ① 개념 → ② 구현 → ③ 테스트 확인 → ④ Study.md 기록. 작은 커밋 단위로 안전하게.
