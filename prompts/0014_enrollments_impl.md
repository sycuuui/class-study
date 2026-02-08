이 디렉토리 범위 안에서만 작업해줘.

작업 목적:
- 동시성 제어가 포함된 수강신청/취소 API를 구현해줘.
- 핵심 목표: 정원 초과 절대 금지(정원 1 남았을 때 100명 동시 요청에도 1명만 성공).

구현할 API:
1) POST /enrollments
   - 요청(JSON): { "studentId": number, "courseId": number }
   - 성공: 201 + JSON 응답(Enrollment 식별자 포함)
   - 실패는 docs/API.md의 error 응답 포맷과 에러 코드 스킴(500/600/700번대)을 따른다.

2) DELETE /enrollments
   - 요청(JSON): { "studentId": number, "courseId": number }
   - 성공: 204 (body 없음)
   - 실패는 docs/API.md 스킴을 따른다.

필수 비즈니스 규칙(이번 단계에서 반드시 구현):
- 정원 초과 방지(동시성 포함)
- 중복 신청 방지

동시성 요구:
- courseId별 lock으로 임계구역 보호
- (가능하면) studentId별 lock도 추가하고 락 순서를 고정해 데드락 회피
- 임계구역에서 다음이 원자적으로 실행되게:
  - 중복 체크 → 정원 체크 → enrolled 업데이트 → enrollment 저장/삭제

제약:
- 실행 전에 변경 계획(수정/생성 파일 목록) 제시
- 이 프롬프트 원문을 prompts/0014_enrollments_impl.md 로 저장
- 불필요한 기능 추가 금지(학점 18, 시간 충돌은 이번 작업에선 구현하지 말고, TODO/문서로만 남겨도 됨)
- 기존 list API 및 /health는 깨지지 않게 유지
- (가능하면) JUnit5로 동시성 테스트 1개 추가:
    "정원 1인 강좌에 100개 동시 신청 -> 성공 1, 실패 99, 최종 enrolled=1"

문서:
- docs/API.md에 /enrollments(POST/DELETE) 명세가 없으면 최소 명세를 추가해줘(기존 문서 스타일 유지).

산출물:
- Enrollment 관련 코드 + (가능하면) 동시성 테스트
