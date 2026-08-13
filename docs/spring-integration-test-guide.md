# Spring 통합 테스트 가이드 — 동시성·규칙·응답구조 테스트

> 이 프로젝트(수강신청)의 D8 테스트를 만들며 배운 내용 복습용 정리.
> 실제 테스트 파일(`src/test/java/com/musinsa/course/domain/enrollment/*`) 기준.

---

## 0. TL;DR

- 테스트는 항상 **given(준비) → when(실행) → then(검증)**.
- `@SpringBootTest` = 앱 통째로 띄워 실제 빈·DB·트랜잭션·락을 검증.
- **동시성 테스트에는 `@Transactional`을 붙이지 않는다** (스레드가 커밋된 데이터를 봐야 하므로) ← 가장 중요.
- 테스트 격리는 `@AfterEach`에서 FK 역순 `deleteAll`.
- 예외 검증은 `assertThatThrownBy(...).extracting(→ErrorCode).isEqualTo(...)`.
- HTTP 응답 봉투는 `MockMvc + jsonPath`.

---

## 1. 테스트 3계층 — 무엇을 어느 레벨로?

| 레벨 | 도구 | 대상 | 속도 |
|---|---|---|---|
| 단위 | 순수 JUnit | validator 등 순수 로직 | 매우 빠름 |
| 슬라이스 | `@DataJpaTest` | 리포지토리 파생쿼리·@EntityGraph | 빠름(JPA만 로드) |
| 통합 | `@SpringBootTest` | Service+트랜잭션+**동시성** | 느림(전체 컨텍스트) |

우리는 동시성·락·트랜잭션이 핵심이라 **통합(@SpringBootTest)** 을 주로 썼다.

---

## 2. 공통 지원 클래스 (EnrollmentTestSupport)

```java
@SpringBootTest              // 스프링 컨텍스트 전체 기동 → 실제 빈/DB/트랜잭션 사용
@ActiveProfiles("test")      // test 프로파일 활성화
abstract class EnrollmentTestSupport {
    @Autowired protected EnrollmentService enrollmentService;
    @Autowired protected CourseRepository courseRepository;
    // ...

    @AfterEach
    void cleanUp() {                       // 매 테스트 후 DB 비우기(격리)
        enrollmentRepository.deleteAll();  // FK 역순: 자식 먼저
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    protected Course saveCourse(...) { return courseRepository.save(Course.builder()...); }
}
```

- **`@SpringBootTest`**: 진짜 앱을 부팅 → `@Autowired`로 실제 서비스/리포지토리 주입 → 진짜 DB 쿼리. 동시성·락은 이 레벨이어야 재현된다.
- **`@ActiveProfiles("test")`**: `src/test/resources/application.yml`(인메모리 H2)을 쓰게 하고, 시더의 `@Profile("!test")` 조건을 꺼서 대량 시드를 막는다.
- **abstract + 상속**: 픽스처를 한 곳에 모으고 실제 테스트가 `extends`. 어노테이션도 상속돼 자식은 다시 안 붙여도 된다.
- **`@AfterEach` FK 역순 삭제**: 없으면 앞 테스트 데이터가 뒤 테스트를 오염. enrollment가 course/student를 참조하므로 enrollment 먼저 삭제.

---

## 3. ★ 동시성 테스트에 @Transactional을 붙이면 안 되는 이유

통합 테스트는 보통 `@Transactional`로 자동 롤백(정리 편의)을 쓴다. 하지만 **동시성 테스트에서는 금지**다.

- 테스트가 `@Transactional`이면 setup 데이터가 **아직 커밋되지 않음** → 내가 띄운 **다른 스레드(각자 별도 트랜잭션)** 는 그 데이터를 **못 본다**.
- 비관적 락은 **커밋 시점**에 풀리는데, 전부 한 트랜잭션으로 묶이면 락 경쟁 자체가 재현되지 않는다.

→ 그래서 **비트랜잭션**으로 두고, 정리는 `@AfterEach` 수동 삭제로 한다.

---

## 4. 동시성 테스트 (EnrollmentConcurrencyTest)

```java
ExecutorService pool = Executors.newFixedThreadPool(32); // 스레드 풀
CountDownLatch start = new CountDownLatch(1);            // 출발 신호
CountDownLatch done  = new CountDownLatch(studentCount); // 종료 대기
AtomicInteger success = new AtomicInteger();             // 스레드-안전 카운터

for (Long studentId : studentIds) {
    pool.submit(() -> {
        try {
            start.await();                               // 다같이 대기하다…
            enrollmentService.requestEnrollment(studentId, courseId); // 각자 트랜잭션
            success.incrementAndGet();
        } catch (Exception e) {
            failure.incrementAndGet();
        } finally {
            done.countDown();
        }
    });
}
start.countDown();                    // …일제히 출발(진짜 동시)
done.await(30, TimeUnit.SECONDS);     // 전부 끝날 때까지 대기

assertThat(success.get()).isEqualTo(capacity);          // 성공 == 정원
assertThat(enrollmentRepository.countByCourseAndDeletedAtIsNull(course)).isEqualTo(capacity);
```

- **`ExecutorService`**: 스레드 풀. `submit(작업)`으로 작업 제출.
- **`CountDownLatch start`**: 모든 스레드가 `start.await()`로 대기 → 메인이 `start.countDown()` 하면 **일제히 출발**. 없으면 순차 실행돼 경쟁이 안 생긴다.
- **`CountDownLatch done`**: 각 스레드가 끝나며 `countDown()`, 메인은 `done.await()`로 전부 끝날 때까지 대기.
- **`AtomicInteger`**: 일반 `int++`는 멀티스레드에서 값이 깨진다(경쟁). `incrementAndGet()`은 원자적이라 안전.
- **의미**: 정원 20 강좌에 60명 동시신청 → `성공==20`, `countByCourse==20`. 비관적 락이 정원을 지켰다는 증거(초과 삽입 0).

---

## 5. 예외/규칙 테스트 (EnrollmentServiceTest) — AssertJ

```java
assertThat(actual).isEqualTo(expected);                 // 값 비교

assertThatThrownBy(() -> enrollmentService.requestEnrollment(...)) // 예외가 나는가
    .isInstanceOf(ApplicationException.class)           // 예외 타입
    .extracting(e -> ((ApplicationException) e).getErrorCode()) // 예외에서 ErrorCode 추출
    .isEqualTo(ErrorCode.DUPLICATE_ENROLLMENT);         // 기대 코드와 일치?

assertThatCode(() -> ...).doesNotThrowAnyException();   // 예외 없이 성공하는가
```

- `assertThatThrownBy`가 예외 테스트의 핵심: "이 코드를 실행하면 **이 예외 + 이 ErrorCode**"를 선언형으로.
- 검증한 규칙: STUDENT/COURSE_NOT_FOUND, DUPLICATE, CAPACITY, SCHEDULE_CONFLICT, CREDIT_LIMIT, 취소 후 재신청, 취소 ENROLLMENT_NOT_FOUND.

---

## 6. HTTP 응답 테스트 (EnrollmentApiTest) — MockMvc

```java
@AutoConfigureMockMvc
class EnrollmentApiTest extends EnrollmentTestSupport {
    @Autowired MockMvc mockMvc;

    mockMvc.perform(post("/enrollments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studentId\":%d,\"courseId\":%d}".formatted(sid, cid)))
        .andExpect(status().isCreated())                 // HTTP 201?
        .andExpect(jsonPath("$.code").value(1001))       // 봉투 code == 1001?
        .andExpect(jsonPath("$.data.status").value("ENROLLED"));
}
```

- **MockMvc**: 톰캣을 실제로 띄우지 않고 DispatcherServlet에 **가짜 요청**을 보내 컨트롤러~예외핸들러까지 통과. 응답 봉투/상태코드 검증에 적합하고 빠름.
- **`jsonPath("$.code")`**: 응답 JSON에서 경로로 값 추출(`$`=루트, `$.data.status`=중첩).
- 검증: 신청 201/1001, 없는학생 404/5000, 취소 200/1000, 파라미터검증 400/2002 → `ApplicationResponse` 봉투 + `GlobalExceptionHandler` 실제 동작 고정.

---

## 7. 테스트 DB 설정 (src/test/resources/application.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000
  jpa:
    hibernate:
      ddl-auto: create-drop
```

- **`h2:mem`**: 인메모리 → 파일 DB(`data/course-db`) 오염 방지, 매번 깨끗한 상태.
- **`create-drop`**: 시작 시 스키마 생성, 종료 시 삭제.
- **`LOCK_TIMEOUT=10000`**: 다수 스레드가 락을 다툴 때 기본 타임아웃이 짧으면 "락 획득 실패" 에러로 실패한다. 10초로 늘려 **차례를 기다리게** 하여 테스트를 안정화.
- 시더 비활성화: `SeedDataGenerator`/`DbSeeder`에 `@Profile("!test")` → 테스트에선 1만 시드 안 돎. 테스트는 필요한 데이터만 직접 준비.

---

## 관련 문서
- `docs/jpa-n+1-and-entitygraph.md` — N+1과 @EntityGraph
- `Study.md`의 **D5**(비관적 락)·**D8**(테스트) 섹션
