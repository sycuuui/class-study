**API 문서**

**공통**
- Base URL: `/`
- 모든 날짜/시간은 로컬 타임존 기준
- 인증 없음. 모든 요청은 `studentId` 등 식별자를 명시적으로 포함한다.
- ID는 정수형

**공통 오류 응답 형식**
```json
{
  "error": {
    "code": 500,
    "message": "에러 메시지",
    "details": {
      "student_id": "studentId",
      "class_id": 123
    }
  }
}
```

**HTTP 상태 코드 및 에러 코드**
- 200 OK: 정상 처리
- 201 Created: 정상 생성(수강신청 성공)
- 204 No Content: 정상 처리(수강취소 성공)
- 400 Bad Request: 요청 파라미터/바디 유효성 오류
- 404 Not Found: 참조 ID 없음
- 409 Conflict: 비즈니스 규칙 위반(정원/학점/시간/중복)
- 503 Service Unavailable: 서버 준비/초기화 미완료

**에러 코드 매핑 표(숫자코드 ↔ 의미 ↔ HTTP 상태 ↔ 기존 코드명(alias))**
- 500 ↔ 요청 파라미터/바디 유효성 오류 ↔ 400 ↔ `INVALID_REQUEST`
- 501 ↔ 학생을 찾을 수 없음 ↔ 404 ↔ `STUDENT_NOT_FOUND`
- 502 ↔ 강좌를 찾을 수 없음 ↔ 404 ↔ `COURSE_NOT_FOUND`
- 503 ↔ 교수를 찾을 수 없음 ↔ 404 ↔ `PROFESSOR_NOT_FOUND`
- 504 ↔ 수강신청 내역 없음 ↔ 404 ↔ `ENROLLMENT_NOT_FOUND`
- 600 ↔ 정원 초과 ↔ 409 ↔ `CAPACITY_EXCEEDED`
- 601 ↔ 학점 초과 ↔ 409 ↔ `CREDIT_LIMIT_EXCEEDED`
- 602 ↔ 시간 충돌 ↔ 409 ↔ `TIME_CONFLICT`
- 603 ↔ 중복 신청 ↔ 409 ↔ `DUPLICATE_ENROLLMENT`
- 700 ↔ 서비스 준비 중 ↔ 503 ↔ (해당 없음)

---

**Health Check**

**GET /health**
- 설명: 데이터 생성이 완료되어 API 호출이 가능할 때만 200 반환

정상 응답 예시
```json
{
  "status": "ok"
}
```

실패 응답 예시
```json
{
  "error": {
    "code": 700,
    "message": "서비스 준비 중",
    "details": {}
  }
}
```

---

**학생 목록 조회**

**GET /students**
- 요청 파라미터
- `limit` (선택, 기본 50)
- `offset` (선택, 기본 0)

정상 응답 예시
```json
{
  "items": [
    {
      "id": 1001,
      "name": "김민준",
      "departmentId": 10,
      "departmentName": "컴퓨터공학과",
      "maxCredits": 18,
      "enrolledCredits": 12
    }
  ],
  "page": {
    "limit": 50,
    "offset": 0,
    "total": 10000
  }
}
```

실패 응답 예시(요청 파라미터 오류)
```json
{
  "error": {
    "code": 500,
    "message": "잘못된 요청 파라미터",
    "details": {}
  }
}
```

---

**교수 목록 조회**

**GET /professors**
- 요청 파라미터
- `limit` (선택, 기본 50)
- `offset` (선택, 기본 0)

정상 응답 예시
```json
{
  "items": [
    {
      "id": 501,
      "name": "박지훈",
      "departmentId": 10,
      "departmentName": "컴퓨터공학과"
    }
  ],
  "page": {
    "limit": 50,
    "offset": 0,
    "total": 100
  }
}
```

실패 응답 예시
```json
{
  "error": {
    "code": 500,
    "message": "잘못된 요청 파라미터",
    "details": {}
  }
}
```

---

**강좌 목록 조회**

**GET /courses**
- 요청 파라미터
- `departmentId` (선택): 학과별 조회
- `limit` (선택, 기본 50)
- `offset` (선택, 기본 0)

정상 응답 예시
```json
{
  "items": [
    {
      "id": 1,
      "name": "자료구조",
      "credits": 3,
      "capacity": 30,
      "enrolled": 25,
      "schedule": "월 09:00-10:30",
      "departmentId": 10,
      "departmentName": "컴퓨터공학과",
      "professorId": 501,
      "professorName": "박지훈"
    }
  ],
  "page": {
    "limit": 50,
    "offset": 0,
    "total": 500
  }
}
```

실패 응답 예시(존재하지 않는 학과 ID)
```json
{
  "error": {
    "code": 500,
    "message": "존재하지 않는 학과 ID",
    "details": {
      "field": "departmentId",
      "value": 9999
    }
  }
}
```

---

**수강신청**

**POST /enrollments**
- 요청 바디(JSON)
```json
{
  "studentId": 1001,
  "courseId": 1
}
```

정상 응답 예시 (201)
```json
{
  "enrollmentId": 900001,
  "studentId": 1001,
  "courseId": 1,
  "status": "enrolled"
}
```

실패 응답 예시
- 정원 초과
```json
{
  "error": {
    "code": 600,
    "message": "정원이 초과되어 신청할 수 없습니다",
    "details": {
      "courseId": 1
    }
  }
}
```
- 학점 초과
```json
{
  "error": {
    "code": 601,
    "message": "최대 학점을 초과합니다",
    "details": {
      "studentId": 1001,
      "maxCredits": 18
    }
  }
}
```
- 시간 충돌
```json
{
  "error": {
    "code": 602,
    "message": "시간표가 충돌합니다",
    "details": {
      "studentId": 1001,
      "courseId": 1
    }
  }
}
```
- 중복 신청
```json
{
  "error": {
    "code": 603,
    "message": "이미 신청한 강좌입니다",
    "details": {
      "studentId": 1001,
      "courseId": 1
    }
  }
}
```
- 존재하지 않는 ID
```json
{
  "error": {
    "code": 502,
    "message": "강좌를 찾을 수 없습니다",
    "details": {
      "courseId": 9999
    }
  }
}
```
```json
{
  "error": {
    "code": 501,
    "message": "학생을 찾을 수 없습니다",
    "details": {
      "studentId": 9999
    }
  }
}
```

---

**수강취소**

**DELETE /enrollments**
- 요청 바디(JSON)
```json
{
  "studentId": 1001,
  "courseId": 1
}
```

정상 응답 예시 (204)
- 바디 없음

실패 응답 예시
- 존재하지 않는 ID
```json
{
  "error": {
    "code": 502,
    "message": "강좌를 찾을 수 없습니다",
    "details": {
      "courseId": 9999
    }
  }
}
```
```json
{
  "error": {
    "code": 501,
    "message": "학생을 찾을 수 없습니다",
    "details": {
      "studentId": 9999
    }
  }
}
```
- 미신청 강좌 취소
```json
{
  "error": {
    "code": 504,
    "message": "수강신청 내역이 없습니다",
    "details": {
      "studentId": 1001,
      "courseId": 1
    }
  }
}
```

---

**내 시간표(이번 학기) 조회**

**GET /timetable**
- 요청 파라미터
- `studentId` (필수)

정상 응답 예시
```json
{
  "studentId": 1001,
  "items": [
    {
      "courseId": 1,
      "name": "자료구조",
      "credits": 3,
      "schedule": "월 09:00-10:30",
      "professorId": 501,
      "professorName": "박지훈"
    }
  ],
  "totalCredits": 12
}
```

실패 응답 예시
```json
{
  "error": {
    "code": 501,
    "message": "학생을 찾을 수 없습니다",
    "details": {
      "studentId": 9999
    }
  }
}
```
