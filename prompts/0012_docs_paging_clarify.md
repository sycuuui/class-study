docs/API.md만 수정해줘(코드 수정 금지).

수정 내용:
1) /students, /professors, /courses의 limit/offset 유효범위 명시:
   - limit: 1~200, 기본 50
   - offset: 0 이상, 기본 0
   - 위반 시 400 + error.code 500
2) /courses의 total은 departmentId 필터 적용 후 총량임을 명시
3) /courses에서 존재하지 않는 departmentId 처리 정책을 문서에 결정 사항으로 명시(추천: 200 + empty)
4) 각 목록 API의 실패 응답에 HTTP 상태코드(400) 명시

이 프롬프트 원문을 prompts/0012_docs_paging_clarify.md 로 저장해줘.
