다음만 구현해줘(불필요한 기능 추가 금지):

목표:
- 서버 시작 시 1분 이내에 초기 데이터를 동적으로 생성해서 메모리에 적재
- /health 는 데이터 준비 완료 후에만 200을 반환(준비 중이면 503)
- 기존 GET /students, /professors, /courses는 하드코딩 List.of(...) 대신
  메모리 저장소에서 조회하도록 변경
- GET /courses?departmentId= (optional) 필터 구현

데이터 규모(최소):
- departments 10+
- professors 100+
- courses 500+
- students 10,000+

데이터 생성 규칙:
- 정적 파일(SQL/CSV) 금지
- "User1", "Course1" 같은 의미 없는 데이터 지양 (현실적인 한글 이름/학과/과목 패턴)
- 소규모 토큰 목록(이름/학과명/과목명)은 코드에 포함 가능
- 고정 시드 사용(재현 가능)

제약:
- 실행 전에 변경 계획 제시
- 이 프롬프트 원문을 prompts/0008_seed_data_generation.md 로 저장
- 빌드/실행 가능 최우선(성능 과도한 작업 금지)
