다음 작업만 해줘(불필요한 기능 추가 금지):

1) GET /students, /professors, /courses 에
   query param limit(기본 50) / offset(기본 0)을 적용해 페이징 조회가 되게 해줘.
2) /courses는 departmentId 필터와 페이징이 함께 동작해야 해.
3) docs/API.md에 limit/offset 파라미터와 예시를 추가해줘.

제약:
- 실행 전에 변경 계획 제시
- 이 프롬프트 원문을 prompts/0010_pagination.md 로 저장
- 응답 포맷(items)은 유지하고, (가능하면) page 정보를 추가하되 최소 변경으로 해줘
