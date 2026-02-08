이 디렉토리 범위 안에서만 작업해줘.

작업 목적:
- 수강신청(POST /enrollments)에서 아래 2개 규칙을 추가 구현해줘.
  1) 학생당 최대 18학점 제한 (초과 시 409 + error code 601)
  2) 시간표 충돌 금지 (충돌 시 409 + error code 602)
- 동시성 요구(정원/중복/학점/시간)가 하나의 임계구역(courseLock → studentLock) 안에서 원자적으로 실행되게 유지해줘.
- 불필요한 기능 추가 금지(새 DB 도입, 인증, 다학기 등 금지). 기존 API 동작/응답 포맷 유지.

구현 가이드(최소 변경):
- InMemoryStore.enroll 내부에서
  - 중복 체크, 정원 체크 다음에
  - "현재 학생의 신청 강좌 credits 합"을 계산하고 신규 강좌 credits 더해 18 초과 시 실패
  - "현재 학생의 신청 강좌 schedule"과 신규 schedule을 파싱해 겹치면 실패
- schedule 파싱은 문자열 포맷 "월 09:00-10:30"만 지원하는 최소 구현으로 하고,
  겹침 조건은 같은 요일 && (startA < endB && startB < endA) 로 판단해줘.
- 에러 응답은 기존 ErrorResponse 포맷 유지하고,
  EnrollmentResult에 601/602 팩토리 메서드(creditLimitExceeded / timeConflict)를 추가해줘.

테스트(가능하면 추가):
- JUnit5로 단위 테스트 2개 추가:
  1) 학점 18 초과 시 601
  2) 시간 충돌 시 602
- 기존 동시성 테스트는 계속 통과해야 함.

문서:
- docs/API.md에 에러코드 601/602 매핑 추가 + /enrollments 실패 예시 2개 추가
- docs/REQUIREMENTS.md에 학점/시간충돌 규칙의 판단 방식(결정사항) 5~10줄로 보강

제약:
- 실행 전에 변경 계획(수정/생성 파일 목록 + 핵심 변경점) 먼저 제시
- 내가 승인하면 실행
- 이 프롬프트 원문을 prompts/0016_credit_time_rules.md 로 저장(원문 그대로, 요약 금지)
- 작업 후 커밋 메시지 추천도 함께 제안해줘
