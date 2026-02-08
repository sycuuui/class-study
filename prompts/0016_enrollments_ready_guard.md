이 디렉토리 범위 안에서만 작업해줘.

목표(불필요 기능 추가 금지):
- 서버 준비 전(ready=false)일 때 /enrollments POST/DELETE가
  docs/API.md와 동일하게 503 + error code 700으로 응답하도록만 보강해줘.
- 성공/실패 응답 포맷(items/error)은 그대로 유지.

제약:
- 실행 전에 변경 계획 제시
- 이 프롬프트 원문을 prompts/0016_enrollments_ready_guard.md 로 저장
- 수정 범위 최소화 (EnrollmentsController + 필요 시 ErrorResponse 재사용)
- 기존 테스트는 깨지지 않게 유지
- docs/API.md에 "/enrollments 준비 전 503(700)" 예시 1개만 추가

작업 끝나면 커밋 메시지 추천도 같이 제안해줘.
