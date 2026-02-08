이 디렉토리 범위 안에서만 작업해줘.

목표(최소 변경):
1) InMemoryStore.enroll에서 학점 제한 하드코딩(18)을 제거하고 student.maxCredits를 사용해줘.
2) Schedule.parse가 실패할 수 있는 경우를 방어해줘:
   - 신규 course.schedule 또는 기존 enrolledCourse.schedule 파싱 실패 시
     서버 예외로 터지지 않게 하고, 에러 코드는 INVALID_REQUEST(500) / HTTP 400으로 응답되게 해줘.
   - 불필요한 기능 추가 금지. 기존 응답 포맷 유지.

제약:
- 실행 전에 변경 계획 제시
- 프롬프트 원문을 prompts/0018_credit_time_hardening.md로 저장
- 작업 후 커밋 메시지 추천
