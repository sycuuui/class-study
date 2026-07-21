# 📚 무신사 수강신청 시스템 — 학습 노트 (Study.md)

> 옛날에 만든 인메모리 수강신청 프로젝트를 다시 분석하며 기술을 학습하는 노트.
> 각 단계마다 **① 코드 해설 → ② 배경 기술 개념 → ③ 확인 질문 & 정답** 순으로 정리.

## 프로젝트 개요

- **정체**: 무신사 백엔드 과제 — 인메모리 수강신청 시스템 (DB 없음)
- **스택**: Java 17 + Spring Boot 3.2.2, Gradle
- **핵심 도전 과제**: 정원 1명 남은 강좌에 100명 동시 신청 → 정확히 1명만 성공 (**동시성 제어**가 주인공)
- **규모**: 서버 시작 시 코드로 동적 생성 — 학생 10,000 / 강좌 500 / 교수 100 / 학과 10+

### 코드 구조

```
com.musinsa.course
├── Application.java              앱 시작점
├── api/                          ← HTTP 계층 (Controller)
│   ├── HealthController          /health (준비 완료 게이트)
│   ├── Students/Courses/Professors  목록 조회 (페이징)
│   ├── EnrollmentsController     수강신청/취소 ★
│   ├── TimetableController       내 시간표
│   └── response/                 공통 응답 DTO (ItemsResponse, ErrorResponse)
└── data/                         ← 핵심 로직 계층
    ├── InMemoryStore.java (412줄) 저장소 + 동시성 + 비즈니스 규칙 ★★★
    ├── SeedData.java             데이터 모델(record)
    └── SeedDataGenerator.java    현실적 데이터 생성
```

### 학습 로드맵

1. **앱 구조 & 요청 흐름** ← ✅ 완료
2. 데이터 생성 (SeedDataGenerator)
3. **동시성 제어 (InMemoryStore.enroll) — 핵심**
4. 비즈니스 규칙 (학점/시간표)
5. 테스트 코드로 동시성 검증

---

# ① 단계: 앱 구조 & 요청 흐름

## 1-1. 앱 시작점 — `Application.java`

```
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- **`@SpringBootApplication`** = 3개 애너테이션의 합침:
  - `@Configuration` — 설정 클래스로 취급
  - `@EnableAutoConfiguration` — `spring-boot-starter-web`이 있으니 내장 톰캣을 8080에 자동 기동
  - `@ComponentScan` — 이 패키지(`com.musinsa.course`) 하위 전체를 스캔해 `@RestController`/`@Component` 등록
- **`SpringApplication.run()`** — 톰캣 기동 → 컴포넌트 스캔 → 객체 생성·연결(DI) → 요청 준비

> 💡 **스프링 컨테이너**: 스프링이 직접 만들어 관리하는 객체 = **Bean(빈)**, 이들이 사는 공간 = **컨테이너(ApplicationContext)**. 우리가 `new` 안 하는 게 포인트.

## 1-2. 계층 구조 (Layered Architecture)

```
[HTTP 요청]
    ↓
api/  ← Controller : HTTP를 아는 곳. 파라미터 검증, 응답 포맷팅
    ↓  (store 호출)
data/ ← Store      : 비즈니스 로직·데이터·동시성. HTTP를 전혀 모름
```

**분리 이유**: Controller는 웹 언어(HTTP status, JSON), Store는 도메인 언어(성공/정원초과). 분리하면 Store만 순수 자바로 테스트 가능 (→ 5단계 동시성 테스트).

## 1-3. 의존성 주입 (DI)

```
private final InMemoryStore store;
public StudentsController(InMemoryStore store) { this.store = store; }
```

- 컨트롤러가 `new InMemoryStore()` 안 함 → 생성자에 "넣어줘"라고 선언만 (**생성자 주입**)
- 스프링이 store를 먼저 만들고 컨트롤러 생성 시 주입
- 빈은 기본 **싱글톤** → 모든 컨트롤러가 **같은 store 하나** 공유 (인메모리 데이터 공유의 이유)
- `final` = 주입 후 못 바꾸게 못 박음

## 1-4. 요청 흐름 — `GET /students?limit=1&offset=0`

```
@GetMapping("/students")                          // ① URL 라우팅
public ResponseEntity<?> list(
    @RequestParam(name="limit",  required=false) Integer limitParam,   // ② 쿼리 파라미터 추출
    @RequestParam(name="offset", required=false) Integer offsetParam
) {
    int limit  = limitParam  == null ? DEFAULT_LIMIT  : limitParam;    // ③ 기본값(50)
    int offset = offsetParam == null ? DEFAULT_OFFSET : offsetParam;
    if (limit < 1 || limit > 200 || offset < 0)                        // ④ 유효성 검사
        return ResponseEntity.badRequest().body(invalidRequest());     //    → 400
    int total = store.getStudents().size();                            // ⑤ store 조회
    List<StudentItem> items = store.getStudents().stream()
        .skip(offset).limit(limit)                                     // ⑥ 페이징
        .map(StudentsController::toItem).toList();                     // ⑦ 내부모델→DTO 변환
    return ResponseEntity.ok(new ItemsResponse<>(                      // ⑧ 200 + JSON
        items, new ItemsResponse.Page(limit, offset, total)));
}
```

- **`@GetMapping`** — GET /students → 이 메서드 매핑. `@RestController`라 리턴값 자동 JSON 변환(Jackson)
- **`@RequestParam(required=false)`** — 안 보내면 null → 삼항으로 기본값 처리
- **유효성 검사** — Controller의 책임: 이상한 값을 Store에 넘기기 전 차단
- **`ResponseEntity`** — status code + body를 함께 담는 HTTP 응답 객체

## 1-5. 응답 DTO는 왜 `record`?

```
public record ItemsResponse<T>(List<T> items, Page page) {
    public record Page(int limit, int offset, int total) {}
}
```

- `record` = 자바 16+, **불변 데이터 클래스**를 한 줄로. 생성자/getter/equals/hashCode/toString 자동 생성
- 응답 JSON: `{ "items": [...], "page": { "limit":1, "offset":0, "total":10000 } }`

> 💡 **DTO 분리**: `StudentItem`(응답용) ↔ `SeedData.Student`(내부 저장용)를 나누면 내부 필드 변경이 API 포맷을 안 흔듦. `toItem()`이 변환 다리.

## 1-6. `/health` — 준비 완료 게이트

```
@GetMapping("/health")
public ResponseEntity<Map<String,Object>> health() {
    if (!store.isReady())                                  // 데이터 생성 중?
        return ResponseEntity.status(SERVICE_UNAVAILABLE)  // → 503 + code 700
            .body(...);
    return ResponseEntity.ok(Map.of("status","ok"));       // 준비됨 → 200
}
```

- 요구사항: 학생 1만 명 데이터 생성 완료 전엔 API 사용 불가
- `store.isReady()` 플래그: 생성 중 503 / 완료 200
- 이 플래그는 `InMemoryStore.setData()` 끝의 `this.ready = true`로 켜짐 (→ 2단계)

## ✅ 1단계 정리

| 개념 | 이 프로젝트에서 |
|---|---|
| Spring Boot 부트스트랩 | `@SpringBootApplication` 한 줄로 톰캣+스캔+DI |
| Bean / 컨테이너 | 컨트롤러·스토어를 스프링이 만들어 관리 |
| 계층 분리 | `api`(HTTP) ↔ `data`(로직) |
| 생성자 주입(DI) | `final` 필드 + 생성자로 store 공유 |
| 요청 매핑 | `@GetMapping` + `@RequestParam` |
| 응답 표현 | `ResponseEntity` + `record` DTO |
| 준비 게이트 | `/health`의 `isReady` 플래그 |

## 🤔 확인 질문 & 정답

**Q1. `StudentsController` 생성자에서 `InMemoryStore`를 주입받지 않고 `new InMemoryStore()`로 직접 만들면 무슨 문제가 생길까?**

- 내 답변: 하나가 아닌 여러 개의 InMemoryStore가 생성되어 다른 곳과 충돌이 일어날 수 있다.
- ✅ 정답 (교정): 방향은 정확함 — 컨트롤러마다 **제각각의 store**가 생겨 데이터가 공유되지 않는다. 더 치명적인 핵심은, 데이터를 채워주는 `SeedDataGenerator`는 **스프링이 관리하는 store**에만 데이터를 주입하므로, 컨트롤러가 직접 `new`로 만든 store는 **아무도 데이터를 안 넣어줘 영원히 비어 있고 `isReady`도 false**로 남는다. → 조회하면 빈 결과, `/health`는 계속 503.

**Q2. `@RestController` 대신 `@Controller`를 쓰면 리턴한 record가 JSON으로 안 나간다. 왜? (`@RestController` = `@Controller` + `???`)**

- 내 답변: ResponseBody를 추가해야 하기 때문. ResponseBody는 JSON 형태의 데이터를 읽을 수 있기 때문(?)
- ✅ 정답 (교정): 빈칸은 **`@ResponseBody`** — 정답. 단, 표현 교정: `@ResponseBody`는 데이터를 "읽는" 게 아니라 **리턴한 객체를 HTTP 응답 본문(body)에 직접 써넣으라(write)** 는 뜻. 이때 Jackson이 자바 객체 → JSON으로 직렬화한다. `@ResponseBody`가 없으면 스프링은 리턴 문자열을 **뷰(HTML) 이름**으로 착각해 해당 화면을 찾으려다 실패한다. 즉 `@RestController` = `@Controller` + `@ResponseBody`.

---

# ② 단계: 데이터 생성 (SeedDataGenerator)

> 요구사항 제약: 정적 파일(SQL/CSV) 금지, 대량 리터럴 금지, 코드로 동적 생성, 현실적 이름, 1분 이내, 학생 1만 명.

## 2-1. 데이터 모델 — `SeedData.java`

```
public record SeedData(List<Department> departments, List<Professor> professors,
                       List<Course> courses, List<Student> students) {
    public record Department(int id, String name) {}
    public record Professor(int id, String name, int departmentId, String departmentName) {}
    public record Course(int id, String name, int credits, int capacity, int enrolled,
                         String schedule, int departmentId, String departmentName,
                         int professorId, String professorName) {}
    public record Student(int id, String name, int departmentId, String departmentName,
                          int maxCredits, int enrolledCredits) {}
}
```

- 4개 도메인 모델 전부 `record` (불변 데이터 클래스). `SeedData`가 이 4개를 한 덩어리로 묶는 컨테이너.
- **비정규화(denormalization)**: `Course`가 `departmentId`뿐 아니라 `departmentName`, `professorName`까지 통째로 보유. DB·JOIN이 없으니 조회 시 바로 쓰라고 이름을 복사해 둠 (인메모리 설계 트레이드오프).

## 2-2. 언제 실행되나 — `ApplicationRunner`

```
@Component
public class SeedDataGenerator implements ApplicationRunner {
    public void run(ApplicationArguments args) {
        SeedData data = generate(start);
        store.setData(data);       // ← store.ready = true 로 바뀌는 순간
    }
}
```

- `ApplicationRunner`는 스프링 부트 콜백. **모든 빈 생성 + 톰캣 준비 완료 직후 `run()`을 1회 자동 호출**.
- 기동 시퀀스:
  ```
  빈 생성(ready=false) → 톰캣 오픈(이때 /health 오면 503) → run() → generate() 1만명 → setData() → ready=true → 200
  ```
- **왜 생성자 아닌 ApplicationRunner?** "서버는 살아있지만 아직 준비 안 됨(503)" 게이트 상태를 명시적으로 표현하기 위해. 요구사항 "health 200 시점 보장"이 이걸로 풀림.

## 2-3. 핵심 트릭 — "조합 폭발"로 대량 리터럴 회피

```
static final String[] LAST_NAMES  = {"김","이",...};   // 10개
static final String[] FIRST_NAMES = {"민준","서연",...}; // 10개
static String[] buildNamePool() {                        // 10 × 10 = 100개
    for (last : LAST_NAMES) for (first : FIRST_NAMES) pool[idx++] = last + first;
}
```

작은 토큰 목록을 곱해서 대량 데이터 생성 (리터럴 20개 → 조합 100개):

| 풀 | 조합 | 결과 |
|---|---|---|
| `NAME_POOL` | 성10 × 이름10 | 100 이름 |
| `COURSE_NAME_POOL` | 과목20 × 접미사4 | 80 강좌명 |
| `SCHEDULE_POOL` | 요일5 × 시간대5 | **25 시간표** |

- `private static final ... = buildXxx()` → **클래스 로딩 시 1회 계산·재사용** (1만 명마다 재조합 안 함).
- ⚠️ **시간표가 25개뿐** → 강좌 500개가 25개를 나눠 쓰니 같은 시간표 강좌가 많음 → 4단계 "시간표 충돌"이 자주 발동하는 이유.

## 2-4. 재현 가능한 난수 — 고정 시드

```
static final long SEED = 42L;
Random random = new Random(SEED);
```

- 시드 고정 → 재시작해도 항상 같은 순서의 난수 → 재현 가능(디버깅·평가 편의). 요구사항 결정 8번.
- `Random`은 스레드 세이프 아님 → but 생성 단계는 단일 스레드라 OK.

## 2-5. ID 대역 분리

```
departments : 10~     professors : 500~599   courses : 1~500
students : 1000~10999  enrollment : 900_000~ (enrollmentSeq)
```

정수 증가 ID를 쓰되 대역을 갈라 사람이 눈으로 종류 구분 가능(평가자 편의). 요구사항 결정 9번.

## 2-6. 현실적인 값의 범위

```
int credits  = 3;                          // 모든 강좌 3학점(단순화)
int capacity = 20 + random.nextInt(41);    // 20~60
int enrolled = random.nextInt(capacity+1); // 0~capacity (이미 일부 찬 상태로 시작, 꽉 찬 강좌도 가능)
int enrolledCredits = random.nextInt(7)*3; // {0,3,...,18}
```

- `enrolled`를 처음부터 랜덤으로 채워 강좌별 여유가 달라짐 → 동시성 테스트가 현실적.

## 2-7. 성능 로깅

```
log.info("seed students ms={}", (t4 - t3));
log.info("seed total ms={}", (t4 - start));
```

- 단계별 소요 ms 로깅으로 "1분 이내" 요구사항 실측 검증.
- **SLF4J Logger**: `System.out.println` 대신 → 로그 레벨 구분, `{}` 자리표시자로 문자열 사전 결합 회피(성능).

## ✅ 2단계 정리

| 개념 | 이 프로젝트에서 |
|---|---|
| record 데이터 모델 | 4개 도메인 + SeedData 컨테이너 |
| 비정규화 | Course가 이름까지 복사 보유 (DB·JOIN 없음) |
| ApplicationRunner | 톰캣 기동 후 자동 실행 → ready ON |
| 조합 폭발 | 토큰 20개 조합 → 대량 데이터, 리터럴 금지 우회 |
| static final 풀 | 클래스 로딩 시 1회 계산·재사용 |
| 고정 시드 | new Random(42) → 재현 가능 |
| ID 대역 분리 | 학과10/강좌1/교수500/학생1000 |
| 성능 로깅 | 단계별 ms로 "1분 이내" 검증 |

## 🤔 확인 질문 & 정답

**Q1. `SCHEDULE_POOL`은 25개뿐인데 강좌는 500개다. 이 사실이 4단계에서 배울 "시간표 충돌" 규칙과 어떤 관련이 있을까?**

- 내 답변: 같은 시간대에 있는 강좌가 많아서, 수강신청 시 같은 시간대에는 하나만 신청할 수 있도록 하기 위함.
- ✅ 정답 (교정): 정확함. 25개 시간표를 500개 강좌가 나눠 쓰니 **같은 시간대 강좌가 수십 개씩 겹침** → 한 학생이 여러 강좌 신청 시 "같은 요일·시간" 강좌를 고를 확률이 높고 그때 **TIME_CONFLICT(409+602)** 규칙이 발동해 막힘. 덧붙이면, 이건 **테스트하기 좋은 설계**다 — 시간표가 다 제각각이면 충돌이 거의 안 나 규칙 검증이 어려운데, 25개로 좁혀 **충돌 상황이 자연히 자주 생기게** 만든 것.

**Q2. `new Random(SEED)`로 시드를 고정하지 않고 `new Random()`(시드 없음)을 쓰면, 이 프로젝트에서 구체적으로 어떤 불편이 생길까?**

- 내 답변: 매번 다른 데이터로 테스트가 진행된다.
- ✅ 정답 (교정): 맞음. 구체적 불편 3가지 — (1) **버그 재현 불가**: 재시작하면 학생 1000번이 다른 학과·상태라 아까 나던 버그가 사라짐. (2) **평가자가 ID 특정 불가**: "강좌 3번은 정원 25명" 같은 걸 문서에 못 적음(재시작마다 바뀜). (3) **테스트 코드 깨짐**: 특정 값 의존 assert가 실행마다 통과/실패 오락가락. → 고정 시드의 진짜 이득은 "**언제 돌려도 똑같은 세계가 재현된다**".

---

# ③ 단계: 동시성 제어 ★ 핵심

> 요구사항: 정원 1자리 남은 강좌에 100명 동시 신청 → 정확히 1명만 성공. 어떤 경우에도 정원 초과 불가.

## 3-0. 문제 — 경쟁 조건(Race Condition)

```
// ❌ 보호 없는 코드 (문제 예시)
if (course.enrolled < course.capacity) {  // ① 확인
    course.enrolled++;                      // ② 실행
}
```

- 스레드 100개가 동시에 밟으면: A가 ①통과 → B도 ①통과(A가 아직 ②전) → A ②→30 → B ②→31 💥 초과.
- 핵심: **확인(check)과 실행(act) 사이가 벌어져** 그 틈에 다른 스레드가 끼어듦 = **check-then-act 경쟁 조건**. (`enrolled++`조차 읽기+더하기+쓰기라 비원자적)
- 해결: "확인~실행"을 **한 번에 한 스레드만** 지나가게 = **임계 구역(critical section)**, 그 도구가 **락(Lock)**.

## 3-1. 락 설계 — ReentrantLock 두 종류

```
private final Map<Integer, ReentrantLock> courseLocks  = new ConcurrentHashMap<>();
private final Map<Integer, ReentrantLock> studentLocks = new ConcurrentHashMap<>();
```

- 강좌 하나당 락 하나 + 학생 하나당 락 하나 (**락 분할, lock striping**).
- 전역 락 하나면 상관없는 강좌끼리도 줄서야 함 → 강좌별로 쪼개 **다른 강좌 신청은 병렬**.
- 두 종류 이유: **강좌 락**=정원 보호(같은 강좌 100명 직렬화), **학생 락**=한 학생의 중복 신청·학점/시간 계산 도중 끼어들기 방지.

## 3-2. 락 생성 — computeIfAbsent

```
ReentrantLock courseLock = courseLocks.computeIfAbsent(courseId, ignored -> new ReentrantLock());
```

- "있으면 그거, 없으면 만들어 넣고 그거" 를 **원자적으로**. → 같은 courseId엔 **모든 스레드가 반드시 같은 락 객체**를 받음.
- 그냥 `if(없으면) put()`이면 두 스레드가 각자 다른 락을 만들어 락이 무의미해짐.
- `ReentrantLock` = "재진입 가능": 같은 스레드가 다시 lock()해도 OK. synchronized보다 세밀(tryLock/타임아웃/공정성).

## 3-3. 데드락 회피 — 락 순서 고정 (백미)

```
courseLock.lock();          // ① 항상 강좌 먼저
try {
    studentLock.lock();     // ② 그 다음 학생
    try { /* 임계 구역 */ }
    finally { studentLock.unlock(); }   // 역순 해제
} finally { courseLock.unlock(); }
```

- 데드락: A(강좌락→학생락 대기) + B(학생락→강좌락 대기) → 서로 영원히 대기 💀.
- 해결: **모두 같은 순서로 잡으면 절대 안 걸림.** 이 프로젝트는 **항상 courseId → studentId** (AGENTS.md 규칙). `cancel()`도 같은 순서 → enroll/cancel 섞여도 안전.
- **try/finally 필수**: 임계구역서 예외 나도 finally가 반드시 unlock → 락 누수(강좌 영구 동결) 방지. 잠근 역순으로 해제.

## 3-4. 임계 구역 내부 — 순서가 곧 정책

```
if (enrollments.contains(key)) return duplicateEnrollment();      // (1) 중복 409+603
if (course.enrolled >= course.capacity) return capacityExceeded(); // (2) 정원 409+600
for (existing : enrollments) {  // (3) 학점 합산 + 시간충돌
    currentCredits += enrolledCourse.credits;
    if (newSchedule.overlaps(existingSchedule)) return timeConflict(); // 409+602
}
if (currentCredits + course.credits > student.maxCredits()) return creditLimitExceeded(); // (4) 409+601
course.enrolled++; enrollmentSeq.incrementAndGet(); enrollments.add(key); ... // (5) 등록
```

- 핵심: **확인(1~4)과 반영(5)이 한 락 안에 통째로** → 3-0의 "틈"이 사라짐 → 100명 와도 한 명씩 통과, 29→30 순간 다음은 (2)에서 막힘 = **정확히 1명 성공**.
- 순서: 중복→정원→시간충돌→학점 (가벼운 검사 먼저, 요구사항 우선순위대로 에러 매핑).

## 3-5. 락 없이도 안전한 자료구조

```
private final Set<EnrollmentKey> enrollments = ConcurrentHashMap.newKeySet();
private final Map<EnrollmentKey, Long> enrollmentIds = new ConcurrentHashMap<>();
private final AtomicLong enrollmentSeq = new AtomicLong(900_000);
private volatile int enrolled;   // CourseState
```

- **왜?** 쓰기(enroll/cancel)는 락으로 보호되지만 **`timetable()`·목록조회는 락 없이** 읽음. 조회 중 다른 스레드가 수정해도 안 깨지게.
- `ConcurrentHashMap.newKeySet` — 동시 읽기·쓰기 안전(일반 HashSet은 순회 중 수정 시 예외/손상).
- `AtomicLong.incrementAndGet()` — 원자적 "증가+반환" → 두 신청이 같은 ID 못 받음.
- `volatile` — 항상 메인 메모리서 최신 읽기(가시성). 락 없이 enrolled 읽는 경로 대비.
- **2중 방어**: 쓰기=락으로 직렬화, 락 없는 읽기=동시성 자료구조+volatile 안전망.

## 3-6. 얇은 컨트롤러

```
@PostMapping("/enrollments")
public ResponseEntity<?> enroll(@RequestBody EnrollmentRequest request) {
    if (!store.isReady()) return ResponseEntity.status(503)...;
    EnrollmentResult result = store.enroll(request.studentId(), request.courseId());
    if (result.success) return ResponseEntity.status(201)...;
    return ResponseEntity.status(result.httpStatus).body(errorBody(result));
}
```

- `@RequestBody` — 요청 본문 JSON → `EnrollmentRequest` record 역변환(Jackson).
- 컨트롤러는 동시성을 전혀 모름. `EnrollmentResult`의 httpStatus/errorCode를 HTTP로 번역만.
- 계층 분리 덕에 동시성 테스트는 톰캣 없이 `store.enroll()`만 직접 호출 (→ 5단계).

## ✅ 3단계 정리

| 개념 | 이 프로젝트에서 |
|---|---|
| 경쟁 조건 (check-then-act) | 정원 확인과 증가 사이 틈 → 초과 |
| 임계 구역 | "확인~반영"을 한 락 안에 |
| 락 분할 | 강좌·학생별 락 → 다른 강좌 병렬 |
| computeIfAbsent | 같은 키엔 항상 같은 락 (원자적 생성) |
| 데드락 회피 | 락 순서 고정 (courseId→studentId) |
| try/finally unlock | 예외 나도 해제 보장 (역순) |
| 동시성 자료구조 | 락 없는 읽기 보호 (newKeySet 등) |
| AtomicLong | ID 발급 원자성 |
| volatile | enrolled 가시성 |
| 얇은 컨트롤러 | 동시성은 store에, api는 번역만 |

## 🤔 확인 질문 & 정답

**Q1. `cancel()`(수강취소)도 락을 잡을 때 반드시 `courseLock` → `studentLock` 순서로 잡는다. 만약 취소만 순서를 반대로(학생→강좌) 짰다면 무슨 일이 벌어질 수 있을까?**

- 내 답변: 학생이 취소했는데 그것이 강좌에 바로 반영되지 않아, 정원이 남았음에도 수강신청 못 하는 경우가 생긴다.
- ✅ 정답 (교정): 핵심은 **데드락(교착)**. enroll은 강좌→학생, cancel만 학생→강좌 순서면 — 스레드 A(enroll)가 강좌C 락 잡고 학생X 락 대기, 동시에 스레드 B(cancel)가 학생X 락 잡고 강좌C 락 대기 → **서로가 가진 걸 영원히 기다려 둘 다 멈춤 💀**. 락을 엇갈린 순서로 잡는 순간 데드락 발생. 그래서 courseId→studentId 순서 규칙이 enroll·cancel **양쪽 모두**에 적용돼야 함. (내 답변의 "취소 반영 지연"은 다른 종류의 문제(가시성/정합성)이며, 실제로는 enrolled가 volatile이고 취소도 락 안에서 `enrolled--` 하므로 발생하지 않음.)

**Q2. 임계 구역 안에서 예외가 나도 락이 풀리도록 `try/finally`로 감쌌다. 만약 `finally` 없이 그냥 `unlock()`만 코드 마지막 줄에 뒀는데, 중간에서 예외가 터졌다면 그 강좌는 어떤 상태가 될까?**

- 내 답변: 예외가 터지면 임계구역에서 빠져나오기 때문에 unlock()을 하지 못하고 나와서 영원히 lock 상태에 빠진다.
- ✅ 정답 (교정): 정확함. 예외로 그 아래 `unlock()`이 실행 안 된 채 메서드를 빠져나가 **락이 잠긴 채 영원히 남음 = 락 누수(lock leak)**. 락을 쥔 스레드는 이미 떠나서 **아무도 풀어줄 수 없음** → 이후 그 강좌에 신청하는 모든 스레드가 `courseLock.lock()`에서 **무한 대기 = 그 강좌 영구 동결**(신청·취소 불가). 다른 강좌는 락이 따로라 멀쩡. → 그래서 락은 거의 무조건 `try/finally`로 "무슨 일이 있어도 반드시 unlock" 보장.

---

# ④ 단계: 비즈니스 규칙 (학점 제한 & 시간표 충돌)

> 규칙 2개: (1) 학생당 최대 18학점 → 409+601, (2) 동일 시간대 중복 불가 → 409+602.
> 둘 다 "신규 강좌 vs 학생이 이미 든 모든 강좌" 비교 → 신청 내역 한 바퀴 순회.

## 4-0. 한 번의 순회로 두 규칙 동시 처리

```java
int currentCredits = 0;
Schedule newSchedule = Schedule.tryParse(course.schedule);
if (newSchedule == null) return invalidRequest();
for (EnrollmentKey existing : enrollments) {
    if (existing.studentId != studentId) continue;      // 이 학생 것만
    currentCredits += enrolledCourse.credits;           // ① 학점 누적
    Schedule existingSchedule = Schedule.tryParse(enrolledCourse.schedule);
    if (newSchedule.overlaps(existingSchedule)) return timeConflict(); // ② 즉시 충돌 검사
}
if (currentCredits + course.credits > student.maxCredits()) return creditLimitExceeded(); // ③ 루프 밖 최종 판정
```

- 학점은 루프서 누적(①), 시간충돌은 강좌마다 즉시(②), 학점 판정은 루프 끝나고 1회(③).

## 4-1. 문자열 → 구조 파싱: `Schedule.tryParse()`

```java
private record Schedule(String day, int start, int end) {
    static Schedule tryParse(String s) {
        String[] parts = s.trim().split("\\s+");   // "월 09:00-10:30" → ["월","09:00-10:30"]
        if (parts.length != 2) return null;
        String[] times = parts[1].split("-");      // → ["09:00","10:30"]
        Integer start = tryToMinutes(times[0]);    // 540
        Integer end   = tryToMinutes(times[1]);    // 630
        if (start==null || end==null || start>=end) return null;
        return new Schedule(parts[0], start, end);
    }
}
```

- 저장은 문자열 `"월 09:00-10:30"`, 계산은 구조 `Schedule("월",540,630)`.
- **시간을 "분"으로 환산**: `HH:MM` → `H*60+M` → 비교가 `<`, `>` 정수 산수로 간단해짐 (540<630).
- `tryToMinutes`: 콜론 위치·숫자여부·범위(0~23, 0~59) 검증, 실패 시 null.

## 4-2. 방어적 파싱 — 실패를 null로

- `try-` 접두사 = "실패하면 예외 대신 null". 잘못된 형식/null 입력도 예외 없이 null → 호출부서 `invalidRequest()`(400).
- **왜 예외 대신 null?** 입력 오류는 "예상 가능한 상황"이라 예외보다 null 체크가 흐름 단순·성능 유리. 단계별로 걸러 null 반환 = **방어적 프로그래밍**.
- (실무 대안: `Optional<Schedule>`. 여기선 단순함 우선.)

## 4-3. 시간 충돌 판정 — `overlaps()` (구간 겹침 공식)

```java
private boolean overlaps(Schedule other) {
    if (!this.day.equals(other.day)) return false;         // 요일 다르면 안 겹침
    return this.start < other.end && other.start < this.end;  // ★ 핵심 공식
}
```

- **`startA < endB && startB < endA`** = 두 구간 겹침 표준 공식.
- 유도: "안 겹침 = (endA<=startB) OR (endB<=startA)". 그 부정(드모르간) = (endA>startB) AND (endB>startA) = 코드와 일치.
- **`<` vs `<=`**: `09:00-10:30` 다음 `10:30-12:00`은 경계만 맞닿고 안 겹침 → `<` 사용(맞닿음 허용, 연강 OK). 구간을 `[start, end)`로 봄. `<=`였다면 연강을 전부 충돌 오판.

## 4-4. 학점 규칙 — 합산과 판정 분리

```java
currentCredits += enrolledCourse.credits;                       // 루프: 기존 합
if (currentCredits + course.credits > student.maxCredits())     // 루프 밖: +신규 > 18?
    return creditLimitExceeded();
```

- 하드코딩 18 대신 `student.maxCredits()` 사용 → 학생별 최대치 변경이 데이터만으로 가능.
- ⚠️ **정합성 단일 출처**: 2단계 `enrolledCredits`(랜덤, 목록 표시용)는 판정에 안 씀. 실제 판정은 항상 `enrollments`(진짜 신청 기록) 순회로 재계산.

## 4-5. 왜 검사가 전부 락 안에 있나 (3단계 연결)

- 락 밖에서 "17학점, 하나 더 OK" 확인 후 락 안에서 등록하면, 그 사이 같은 학생의 다른 신청이 끼어들어 18 초과 가능 (또 다른 check-then-act).
- **학생 락**으로 한 학생 신청 직렬화 → 읽기(합산)→판정→등록을 원자적으로. → 학생 락의 진짜 존재 이유가 여기서 완성.

## ✅ 4단계 정리

| 개념 | 이 프로젝트에서 |
|---|---|
| 문자열→구조 파싱 | "월 09:00-10:30" → Schedule("월",540,630) |
| 분 환산 | HH:MM → H*60+M → 비교가 산수 |
| 방어적 파싱 | 잘못된 입력은 예외 대신 null → 400 |
| 구간 겹침 공식 | startA<endB && startB<endA (드모르간 유도) |
| 경계 < vs <= | 맞닿음 허용(연강 OK), [start,end) 관점 |
| 학점 합산/판정 분리 | 루프 누적, 루프 밖 판정, maxCredits() |
| 정합성 단일 출처 | 표시용 enrolledCredits ≠ 판정용 enrollments 순회 |
| 규칙도 락 안에서 | 학생 단위 check-then-act 방지 |

## 🤔 확인 질문 & 정답

**Q1. `overlaps()`에서 `<` 대신 `<=`를 썼다면, `09:00-10:30` 강좌를 든 학생이 `10:30-12:00` 강좌를 신청할 때 어떤 결과가 나올까? 그게 왜 문제일까?**

- 내 답변: 10:30 강좌를 수강신청 못함. 수강 가능한데도 못하게 됨.
- ✅ 정답 (교정): 정확함. `<=`면 경계값 10:30에서 충돌로 오판 → 409 TIME_CONFLICT로 **신청 거부**. 문제는 이 둘이 **시간이 안 겹치는 연강(연속 수업)** 이라 원래 같이 들을 수 있어야 한다는 것 → **정상 신청을 막는 버그**. 그래서 `<`로 "끝시각=시작시각" 맞닿음은 허용.

**Q2. 학점 판정에서 학생 객체의 `enrolledCredits`(2단계에서 랜덤으로 넣은 값)를 쓰지 않고, 굳이 `enrollments`를 순회해서 다시 학점을 합산하는 이유는?**

- 내 답변: 진짜 판정을 하기 위해서. 정합성을 보장하기 위해서.
- ✅ 정답 (교정): 맞음. 구체화하면 — `enrolledCredits`는 2단계에서 **랜덤으로 넣은 표시용 값**이라 실제 신청 강좌들의 학점 합과 일치 보장이 없고, 신청/취소해도 **자동 갱신되지 않음**(갱신 로직 없음). 이걸 믿으면 **틀린 값으로 18학점 제한 검사**. 반면 `enrollments`(실제 신청 기록) 순회는 **항상 현재 진짜 상태** 반영 = **정합성의 단일 출처(single source of truth)**.

---

# ⑤ 단계: 테스트 코드로 동시성 검증

> 지금까지 "이론상 정원 초과 안 남"을 배움 → 이 단계는 그걸 실제 코드로 증명.

## 5-0. 테스트가 store를 직접 만드는 이유 (계층 분리의 보상)

```java
InMemoryStore store = new InMemoryStore();   // 스프링·톰캣·HTTP 없음
store.setData(new SeedData(...));            // 원하는 데이터만 직접 심음
```

- 동시성 로직이 전부 `InMemoryStore`(순수 자바)에 있어 **스프링 컨텍스트/HTTP 없이** `store.enroll()` 직접 호출 → 빠름.
- 💡 1단계 Q1과 연결: 테스트는 컨테이너 **밖**이라 DI 없음 → 직접 `new` + `setData()`가 맞음. 앱 코드는 컨테이너 **안**이라 주입받아야 SeedDataGenerator가 채운 store 공유. 같은 `new`인데 맥락이 정반대.

## 5-1. 시나리오 — "정원 1, 학생 100"

```java
new SeedData.Course(1, "자료구조", 3, /*capacity=*/1, /*enrolled=*/0, ...)   // 정원 1
for (i<100) students.add(new Student(1000+i, ...));                         // 서로 다른 학생 100명
```

- 학생을 다 다르게 → 중복(603)이 아닌 **순수 정원 경쟁(600)** 만 테스트. 100명이 courseId=1에 몰림.

## 5-2. 핵심 — CountDownLatch로 "동시에 출발"

```java
CountDownLatch start = new CountDownLatch(1);    // 출발 총성
CountDownLatch done  = new CountDownLatch(100);  // 완주 확인
for (i<100) executor.submit(() -> {
    start.await();                  // ★ 모든 스레드가 여기서 대기
    result = store.enroll(studentId, 1);
    done.countDown();               // 끝나면 1 깎음
});
start.countDown();                  // ★ 총성! 100개 동시 출발
done.await(10, SECONDS);            // 다 끝날 때까지 대기
```

- `CountDownLatch` = "카운트 0 될 때까지 기다리는 문". 두 개를 반대로 사용.
- **start(1)**: 100 스레드를 `await()`에 대기시켰다가 `countDown()`으로 **일제히 출발** → 진짜 락 경쟁 발생. (단순 반복 호출은 앞 스레드가 이미 끝나 진짜 동시가 아님)
- **done(100)**: 각자 끝날 때 1씩 깎고, 메인은 100개 다 끝날 때까지 대기 (없으면 테스트가 먼저 끝나 결과 못 봄).
- `ExecutorService`(`newFixedThreadPool(100)`) = 스레드 풀. `submit()`으로 작업 던지고 `shutdownNow()`로 정리.

## 5-3. AtomicInteger로 안전한 카운트

```java
AtomicInteger success = new AtomicInteger();
if (result.success) success.incrementAndGet();   // 100 스레드 동시 증가
```

- 평범한 `int++`면 3단계 경쟁 조건으로 개수 유실 → `AtomicInteger`로 원자적 증가. (테스트조차 동시성을 제대로 다뤄야 정확)

## 5-4. 3중 assert — "정확히 1명"

```java
assertEquals(1,  success.get());   // ① 성공 1명
assertEquals(99, failure.get());   // ② 실패 99명 (①+②=100, 유실 없음도 검증)
assertEquals(1,  enrolled);        // ③ 강좌 실제 enrolled도 1 (내부 상태까지)
```

- 락이 없었다면 success=2 / enrolled=31 등으로 **테스트 실패** → 버그 포착. "어떤 경우에도 정원 초과 불가"를 기계적으로 보증.

## 5-5. 규칙 테스트 (EnrollmentRulesTest, 단일 스레드)

**학점 초과(601):**
```java
for (courseId=1..6) store.enroll(1000, courseId);   // 요일 다 다름 → 충돌 없이 6×3=18학점
result = store.enroll(1000, 7);                       // 21학점 시도
assertEquals(601, result.errorCode);
```
- 강좌1("월 09:00-10:30")과 강좌6("월 10:30-12:00")은 같은 월요일이지만 맞닿기만 하고 안 겹침(4단계 Q1) → 둘 다 신청됨. 테스트 데이터가 `<` vs `<=` 경계를 은근히 검증.

**시간 충돌(602):**
```java
Course(1, "월 09:00-10:30"), Course(2, "월 10:00-11:00")   // 10:00~10:30 겹침
store.enroll(1000, 1);
result = store.enroll(1000, 2);
assertEquals(602, result.errorCode);   // overlaps: 540<660 && 600<630 = true
```

## ✅ 5단계 정리

| 개념 | 이 프로젝트에서 |
|---|---|
| 계층 분리의 보상 | store만 new로 떼서 스프링·HTTP 없이 테스트 |
| 시나리오 설계 | 정원 1 + 학생 100 (순수 정원 경쟁) |
| CountDownLatch(start) | 대기 후 일제히 출발 → 진짜 동시성 |
| CountDownLatch(done) | 모두 끝날 때까지 메인 대기 |
| ExecutorService | 스레드 풀로 100개 관리 |
| AtomicInteger | 동시 카운트 유실 없이 셈 |
| 3중 assert | 성공1+실패99+enrolled1 |
| 규칙 테스트 | 601(학점)·602(시간) 단일 스레드 검증 |

## 🤔 확인 질문 & 정답

**Q1. `start` 래치(CountDownLatch) 없이 그냥 for문에서 바로 `store.enroll()`을 100번 호출하면, 이 테스트가 "동시성"을 제대로 검증한다고 볼 수 있을까? 왜?**

- 내 답변: (작성 예정)
- 정답: (답변 후 교정 예정)

**Q2. 마지막 assert에서 `success=1`, `failure=99`만 확인하지 않고 굳이 강좌의 실제 `enrolled==1`까지 추가로 확인하는 이유는?**

- 내 답변: (작성 예정)
- 정답: (답변 후 교정 예정)

---

# 🎓 전체 학습 완료 — 큰 그림

```
요청 흐름 (1단계)
  Controller[HTTP 번역] ──→ InMemoryStore[로직·동시성]
        │                          │
   준비 게이트(/health)      락(강좌+학생, 순서고정) ← 데드락 회피 (3단계)
        │                    임계구역: 중복→정원→시간→학점 (3·4단계)
  ApplicationRunner로              │
  1만명 동적 생성 (2단계)     동시성 자료구조 + volatile
        │                          │
        └──────── 테스트로 "정원 1, 100명 → 1명" 증명 (5단계)
```

**한 문장 요약**: DB 없이 인메모리로, check-then-act 경쟁 조건을 락으로 막아 정원·학점·시간 규칙을 원자적으로 지킨다.

## 핵심 기술 키워드 총정리
- **Spring Boot**: @SpringBootApplication, DI(생성자 주입), @RestController, @GetMapping/@PostMapping, @RequestParam/@RequestBody, ResponseEntity, ApplicationRunner
- **Java 문법**: record, Stream, static final 초기화, 팩토리 메서드, 방어적 파싱(try-/null)
- **동시성**: ReentrantLock, 락 분할(striping), 락 순서 고정(데드락 회피), try/finally unlock, computeIfAbsent, ConcurrentHashMap/newKeySet, AtomicLong/Integer, volatile
- **테스트**: JUnit5, CountDownLatch, ExecutorService(스레드풀), 동시성 검증
- **설계**: 계층 분리(api/data), 준비 게이트, 정합성 단일 출처, 에러 코드 체계
- **알고리즘**: 시간 구간 겹침(startA<endB && startB<endA), 시간의 분 환산

---

# 🏭 실무 리팩토링 (H2 DB 도입) — 구현 로그

> 브랜치 `feat/h2-migration`. 설계: `docs/DB_ARCHITECTURE.md`.

## D1. 프로젝트 셋업 (JPA + H2) ✅

**바꾼 파일 3개:**
- `build.gradle`: 의존성 2줄 추가
  - `implementation 'org.springframework.boot:spring-boot-starter-data-jpa'` — Hibernate(ORM) + Spring Data JPA + @Transactional
  - `runtimeOnly 'com.h2database:h2'` — H2 DB. **runtimeOnly**인 이유: 코드는 H2를 직접 import 안 하고 JPA 표준에만 의존 → 런타임에만 필요. (DB 교체 시 이 한 줄만 바꾸면 됨 = 인터페이스 의존 원칙)
- `src/main/resources/application.yml`: **신규 생성**
  - `url: jdbc:h2:file:./data/course-db` — **파일 모드**(재시작해도 유지). mem:이면 소멸.
  - `ddl-auto: update` — 엔티티 보고 테이블 자동 생성/수정. (실무 운영은 validate + Flyway 권장, 학습은 update)
  - `show-sql` + `format_sql` — JPA가 날리는 실제 SQL을 콘솔에 출력(학습용). D5의 SELECT FOR UPDATE 확인에 활용 예정.
  - `h2.console.enabled: /h2-console` — 브라우저로 DB GUI 확인.
- `.gitignore`: `data/` 추가 — 로컬 DB 파일은 커밋 제외.

**의존성 스코프 3종**: implementation(컴파일+런타임), runtimeOnly(런타임만), testImplementation(테스트만).

**검증 결과**: 빌드 성공 / HikariPool→H2 연결 성공 / H2 콘솔 활성화 / `data/course-db.mv.db` 생성 / `/health` 200(기존 API 무손상) / `data/` git 무시 확인.
