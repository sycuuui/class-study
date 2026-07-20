# 🏛 H2 DB 도입 아키텍처 설계 (Target Design)

> 목표: 인메모리 수강신청 시스템을 **H2 DB + JPA + 레이어드 아키텍처**로 재설계.
> 실무에서 실제 쓰는 방식(DB 트랜잭션/락 기반 동시성 제어)을 학습.

## 확정 설계 결정 (추천안)

| 결정 | 선택 | 이유 |
|---|---|---|
| 동시성 제어 | **비관적 락** (SELECT FOR UPDATE) | 기존 ReentrantLock 임계구역과 사고방식 동일, 연속성 최고 |
| H2 모드 | **파일 모드**(실행) + **인메모리**(테스트) | 실행 시 영속성, 테스트 격리 |
| 데이터 접근 | **Spring Data JPA** | 실무 표준, ORM·연관관계·락 학습 최적 |

## 목표 아키텍처

```
Controller (REST, @Valid, 얇게)   ← HTTP만
   ↓
Service (@Transactional, 비즈니스 규칙)  ← 트랜잭션 경계 = 임계구역
   ↓
Repository (Spring Data JPA)      ← 쿼리
   ↓
Entity ↔ H2 Database             ← 실제 테이블
```

### 인메모리 → DB 전환 매핑

| 지금 (인메모리) | 새 설계 (H2 + JPA) |
|---|---|
| ReentrantLock (프로세스 락) | DB 락 (트랜잭션 + row lock) |
| ConcurrentHashMap 저장 | 테이블 |
| enrollments.contains() 중복 체크 | DB UNIQUE 제약 (student_id, course_id) |
| try/finally lock/unlock | @Transactional 자동 커밋/롤백 |
| 재시작 시 소멸 | 파일 모드로 영속 |

## ERD

```
department (id, name)
professor  (id, name, department_id FK)
student    (id, name, max_credits, department_id FK)   ← enrolledCredits 제거
course     (id, name, credits, capacity,
            schedule_day, schedule_start_min, schedule_end_min,  ← 시간표 구조화 저장
            department_id FK, professor_id FK)
enrollment (id, student_id FK, course_id FK, created_at,
            UNIQUE(student_id, course_id))              ← 중복신청 DB 차단
```

### 설계 포인트
- **enrollment 테이블 + UNIQUE 제약** → 중복 체크를 DB가 담당.
- **schedule 구조화 컬럼**(요일/시작분/종료분) → 매 신청 시 문자열 파싱 불필요.
- **enrolled는 저장 안 하고 COUNT(enrollment)로 계산** → 정합성 단일 출처를 DB 레벨로.
- **student.enrolledCredits 제거** → 죽은 필드 정리.

## 동시성: DB 락으로 "정원 1자리 100명" 차단

```java
@Transactional
public EnrollmentResult enroll(Long studentId, Long courseId) {
    // 락 순서 고정: student → course (데드락 회피)
    Student s = studentRepo.findByIdForUpdate(studentId);   // 학생 row 락
    Course  c = courseRepo.findByIdForUpdate(courseId);     // 강좌 row 락
    if (enrollmentRepo.countByCourseId(courseId) >= c.getCapacity())
        throw new CapacityExceededException();
    // 학점 합산 + 시간 충돌 검사 (student의 기존 enrollment 조회)
    enrollmentRepo.save(new Enrollment(s, c));  // UNIQUE 제약이 중복 차단
    return success(...);
}   // 커밋 = 락 해제
```

**개념 매핑 (3단계 → DB)**: 임계구역=트랜잭션, 락 순서 고정=row 락 순서, try/finally unlock=@Transactional 자동 커밋/롤백. 개념은 같고 도구만 DB로.

## 로드맵 (DB 버전)

| 단계 | 내용 | 핵심 학습 |
|---|---|---|
| D1 | 셋업: build.gradle에 JPA·H2, application.yml, H2 콘솔 | 스프링 DB 설정 |
| D2 | 엔티티 + Repository 설계 (연관관계, 제약) | JPA 매핑, @Entity, @ManyToOne |
| D3 | 시드 데이터를 DB로 (1만 건 배치 insert, 1분 이내) | 배치 처리, 성능 |
| D4 | 조회 API를 JPA로 (페이징 → Pageable) | Spring Data 쿼리 |
| D5 | 수강신청/취소 = 트랜잭션 + 비관적 락 ★ | @Transactional, @Lock, 격리수준 |
| D6 | ErrorCode enum + @RestControllerAdvice 전역 예외 | 예외 표준화 |
| D7 | Bean Validation (@Valid) | 선언적 검증 |
| D8 | 테스트 보강 (@DataJpaTest, 동시성 재검증) | DB 테스트 |
| D9 | 마무리 (OpenAPI, Actuator, 프로파일 분리) | 운영 |

진행 방식: 각 단계 ① 개념 → ② 구현 → ③ 테스트 확인 → ④ Study.md 기록. 작은 커밋 단위.

## 📋 작업 규칙 (협업 방식)

1. **커밋 단위 진행**: 파일 변경은 의미 있는 git commit 단위로 쪼갬. 작업 끝나면 **커밋 메시지를 제안**하고 **실행은 사용자가** 직접 (AI는 커밋하지 않음). 메시지는 프로젝트 컨벤션(feat/docs/prompt 등) 준수.
2. **학습 우선**: 코드 수정 전에 **먼저 개념·이유를 설명하고 학습** → 사용자가 방향을 **완벽히 이해·확인한 뒤에** 실제 수정. 이해 없이 코드부터 짜지 않음.
```
