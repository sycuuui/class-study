# JPA N+1 문제와 @EntityGraph — 완전 정복

> 이 프로젝트(수강신청 리팩토링)에서 `/courses`, `/professors` 목록 조회를 만들며 겪은 N+1 문제와 해결 과정을 복습용으로 정리한 문서.
> 실제 코드(`CourseItem`, `CourseRepository`)와 실제 SQL 로그 기반.

---

## 0. TL;DR (한 줄 요약)

> **N+1** = LAZY 연관을 목록 반복 중에 하나씩 만져서 쿼리가 `1+N`번 나가는 것.
> **원인** = LAZY 프록시의 값을 반복문 안에서 처음 접근 → 그때마다 SELECT.
> **해결** = 그 쿼리에서만 **`@EntityGraph`(fetch join)** 로 연관을 한 번에 끌어옴. to-one이라 페이징과도 안전.

---

## 1. N+1이 뭐냐 — 정의

> 목록 N건을 가져오려고 쿼리 1번 날렸는데, 각 건의 **연관 데이터**를 채우느라 쿼리가 N번 더 나가는 것. 총 **1 + N번**.

### 우리 예시로 계산

`GET /courses?limit=50` 요청 시, `CourseItem`은 `departmentName`, `professorName`이 필요:

| 단계 | 쿼리 수 |
|---|---|
| course 50개 조회 | 1 |
| 각 course의 department 조회 | + 50 |
| 각 course의 professor 조회 | + 50 |
| **합계** | **101** (= 1 + N + N) |

`ProfessorItem`은 연관이 `department` 하나뿐이라, `/professors?limit=100`이면 **1 + 100 = 101번**.

---

## 2. 왜 일어나는가 — LAZY 프록시의 정체

### 문제의 뿌리: `fetch = LAZY`

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

`LAZY` = **"이 연관은 지금 DB에서 안 가져온다. 진짜 쓸 때 가져와라."**

그래서 `courseRepository.findAll(pageable)`은:

```sql
select * from course limit 50   -- department/professor는 안 건드림
```

이때 `course.getDepartment()`가 돌려주는 건 **진짜 Department가 아니라 가짜 대역(프록시)**.
안에 **id만** 있고 나머지 필드는 비어있는 껍데기.

### 조회가 터지는 정확한 순간

껍데기의 **실제 값을 처음 만지는 순간** DB 조회가 발사된다. 우리 코드에서 그 지점:

```java
public static CourseItem from(Course course) {
    return new CourseItem(
        course.getId(),                      // OK (프록시가 이미 id 보유 → 쿼리 없음)
        course.getName(),                    // OK (course 본체는 이미 로딩됨)
        ...
        course.getDepartment().getId(),      // OK (프록시가 id 보유)
        course.getDepartment().getName(),    // 💥 name은 껍데기에 없음 → SELECT department 발사
        course.getProfessor().getId(),
        course.getProfessor().getName()      // 💥 SELECT professor 발사
    );
}
```

- `getId()`는 프록시가 이미 갖고 있어 쿼리가 **안** 나감.
- `getName()`은 껍데기에 없으니 **그제서야** DB를 때림.

### 반복문이 N배로 증폭

이 `from()`이 목록 전체에 대해 반복된다:

```java
return page.map(CourseItem::from);   // 50개 각각 from() 실행
```

**50번 반복** × (department 1 + professor 1) = **쿼리 100번 추가**. → N+1 완성.

### 왜 "조용한" 킬러인가

- 테스트 데이터 3건 → 쿼리 7번. 눈치 못 챔.
- 운영 500건 → **1001번**. DB 왕복 폭발 → 응답 지연.
- 코드에 `for`문 `SELECT`가 없음(숨은 반복 쿼리). **리뷰로도 잘 안 잡힘.**

---

## 3. 곁가지 함정 — `@Transactional`이 없으면 아예 터진다

LAZY는 **세션(영속성 컨텍스트)이 살아있을 때만** 지연 로딩 가능.

- Service에 `@Transactional(readOnly = true)` → 메서드 끝까지 세션 유지 → 그 안에서 `from()` 실행 → LAZY 접근 OK (**대신 N+1 발생**).
- 세션이 닫힌 **Controller에서** `from()` 실행 → 프록시가 DB에 못 물어봄 → **`LazyInitializationException`** 폭사.

그래서 **DTO 변환을 Service 트랜잭션 안에** 둔다.

> ⚠️ 이 둘은 **별개의 문제**다:
> - `LazyInitializationException` = "터지느냐 마느냐"
> - `N+1` = "터지진 않는데 느리냐"

| DTO 변환 위치 | 연관 로딩 전략 | 결과 |
|---|---|---|
| Controller (세션 밖) | LAZY | ❌ LazyInitializationException |
| Service (세션 안) | LAZY | ⚠️ 동작하지만 N+1 |
| Service (세션 안) | **fetch join / EntityGraph** | ✅ 동작 + 쿼리 1번 |

우리는 **세 번째**로 갔다.

---

## 4. 어떻게 제거했나 — @EntityGraph

### 적용한 코드

```java
public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findByDepartment_Id(Long departmentId, Pageable pageable);
}
```

`@EntityGraph`는 Hibernate에게 **"이 쿼리 실행할 때 department·professor도 같이 JOIN해서 한 방에 채워와"** 라고 지시.
→ 프록시가 아니라 **값이 다 찬 진짜 객체**가 옴 → `from()`에서 `getName()` 만져도 **추가 쿼리 0**.

### 실제로 나간 SQL (로그 확인)

```sql
select ...
from course c1_0
  left join department d1_0 on d1_0.id = c1_0.department_id   -- EntityGraph가 만든 조인
  join professor p1_0 on p1_0.id = c1_0.professor_id
```

쿼리 **한 번**에 course + department + professor 전부 실림 → **N+1(101) → 1**.

### ★ 왜 페이징과 같이 써도 안전했나 (핵심 포인트)

- `department`, `professor`는 **to-one** (한 강좌 → 한 학과 / 한 교수). JOIN해도 **행 수가 안 늘어남**. course 50개 → 결과 50행. `LIMIT 50`이 DB에서 정확히 먹음.
- 만약 **to-many** (예: 한 강좌 → 여러 수강생 컬렉션)를 fetch join + 페이징 하면? JOIN으로 행이 뻥튀기 → DB `LIMIT`이 의미 상실 → Hibernate가 **전부 읽어 메모리에서 페이징**하며 **`HHH000104`** 경고. → to-many는 fetch join + 페이징을 피하고 `@BatchSize`/별도 조회로 해결.
- 우리 케이스는 to-one이라 이 함정에 안 걸림. ✅

---

## 5. 다른 해결책 비교

| 방법 | 방식 | 메모 |
|---|---|---|
| **@EntityGraph** ✅ | 파생 쿼리에 어노테이션만 얹음 | 채택. 선언적, 기존 `findByDepartment_Id`에도 그대로 적용 |
| `@Query("... join fetch")` | JPQL에 fetch join 직접 작성 | 동작 동일. 쿼리를 손으로 다 써야 함 |
| `@BatchSize(size=100)` | LAZY 유지하되 IN절로 묶어 조회 | `1+N → 1+(N/배치)`. **to-many N+1 완화에 특히 유용** |
| `fetch = EAGER`로 변경 | 항상 즉시 로딩 | ❌ 안티패턴. 이 연관이 필요 없는 다른 쿼리에서도 항상 조인 → 전역 오염 |

> **원칙**: 연관은 기본 **LAZY**로 두고, N+1이 문제되는 **그 쿼리에서만** fetch join/EntityGraph로 콕 집어 해결한다. **EAGER로 도망가지 않는다.**

---

## 6. 어떻게 탐지하나 (실무 습관)

1. **SQL 로그 켜기**
   ```properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=debug
   ```
   목록 1건 요청에 select가 우수수 → N+1 의심.
2. **쿼리 카운트 어서션**: 테스트에서 `datasource-proxy` 또는 Hibernate `Statistics`로 "이 API는 쿼리 N번 이하" 검증 → 회귀 방지.
3. **패턴 눈에 익히기**: `.map(Dto::from)` / `for (x : list) x.getYyy()` 안에서 **LAZY 연관의 getter**를 만지면 거의 항상 N+1.

---

## 7. 자가 점검 퀴즈

<details>
<summary>Q1. professors 100개를 EntityGraph 없이 조회하면 쿼리는 총 몇 번?</summary>

**101번** (목록 1 + 각 professor의 department 100). ProfessorItem은 연관이 department 하나뿐.
</details>

<details>
<summary>Q2. from()을 Controller(세션 밖)에서 호출하면 N+1이 날까, 아니면 다른 일이 날까?</summary>

N+1 이전에 **`LazyInitializationException`**으로 아예 터진다. 세션이 닫혀 프록시가 DB에 물어볼 수 없기 때문. N+1은 "세션이 살아있어서 조회는 되는데 느린" 상황.
</details>

<details>
<summary>Q3. 한 강좌의 "수강생 목록"(to-many)을 fetch join + 페이징하면?</summary>

행이 뻥튀기돼 DB `LIMIT`이 무의미해지고, Hibernate가 전부 읽어 **메모리에서 페이징**하며 `HHH000104` 경고. to-one과 달리 안전하지 않음. → `@BatchSize`나 별도 조회로 푼다.
</details>

---

## 관련 문서
- `Study.md` 의 **D4-b** 섹션 — courses/professors 마이그레이션 맥락
- 다음 단계: **D5 (수강신청/취소 + 비관적 락)** — 여기서 enrollment 동시성 제어를 다룸
