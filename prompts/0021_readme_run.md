› 이 디렉토리 범위 안에서만 작업해줘.

  작업 목적:
  - README.md에 “실행 방법” 섹션을 보강해줘.
  - 현재 구현된 API들(/students, /professors, /courses, /enrollments, /timetable, /health)이
    로컬에서 어떻게 실행·테스트되는지 명확히 드러내는 것이 목적이다.

  작성 범위/규칙:
  - README.md만 수정한다.
  - 기존 내용은 삭제/변경하지 말고,
    "## 실행 방법" 또는 유사한 섹션이 있으면 그 아래를 보강하고,
    없으면 새 섹션을 추가한다.
  - 코드 변경, 설정 파일 변경, 새 기능 추가는 금지.

  실행 방법에 반드시 포함할 내용:
  1) 필수 환경
     - Java 21
     - Gradle Wrapper 사용
  2) 서버 실행 방법
     - ./gradlew bootRun
     - 서버가 8080 포트에서 실행됨을 명시
  3) 서버 준비 상태 확인
     - GET /health
     - 준비 전/후 응답 차이 간단 설명
  4) 주요 API 간단 테스트 예시 (curl 기반, 1~2개만)
     - GET /students
     - POST /enrollments (예시 JSON 포함)
  5) 테스트 실행 방법
     - ./gradlew test

  문체/스타일:
  - 과제 제출용 README에 어울리는 간결하고 명확한 문체
  - 장황한 설명 금지
  - 명령어는 코드 블록으로 표현

  제약:
  - 이 프롬프트 원문을 prompts/0021_readme_run.md 로 저장
  - README.md 외 다른 파일은 수정하지 말 것
