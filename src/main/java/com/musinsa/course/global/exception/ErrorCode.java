package com.musinsa.course.global.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

    // 1000: Success Case
    SUCCESS(HttpStatus.OK, 1000, "정상적인 요청입니다."),
    CREATED(HttpStatus.CREATED, 1001, "정상적으로 생성되었습니다."),

    // 2000: Common Error
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 2000, "예기치 못한 오류가 발생했습니다."),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, 2001, "존재하지 않는 리소스입니다."),
    INVALID_VALUE_EXCEPTION(HttpStatus.BAD_REQUEST, 2002, "올바르지 않은 요청 값입니다."),
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, 2003, "권한이 없는 요청입니다."),
    ALREADY_DELETE_EXCEPTION(HttpStatus.BAD_REQUEST, 2004, "이미 삭제된 리소스입니다."),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, 2005, "인가되지 않는 요청입니다."),
    ALREADY_EXIST_EXCEPTION(HttpStatus.BAD_REQUEST, 2006, "이미 존재하는 리소스입니다."),
    INVALID_SORT_EXCEPTION(HttpStatus.BAD_REQUEST, 2007, "올바르지 않은 정렬 값입니다."),

    // 3000: Auth Error
    WRONG_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, 3002, "유효하지 않은 토큰입니다."),
    LOGOUT_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, 3003, "로그아웃된 토큰입니다"),
    WRONG_TOKEN(HttpStatus.UNAUTHORIZED, 3004, "유효하지 않은 토큰입니다."),

    // 5000: Student Error
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, 5000, "존재하지 않는 학생입니다."),

    // 6000: Course Error
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, 6000, "존재하지 않는 강좌입니다."),

    // 7000: Enrollment Error
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 7000, "존재하지 않는 수강 신청입니다."),
    DUPLICATE_ENROLLMENT(HttpStatus.CONFLICT, 7001, "이미 신청한 강좌입니다."),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, 7002, "수강 정원을 초과했습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, 7003, "시간표가 겹칩니다."),
    CREDIT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, 7004, "수강 가능 학점을 초과했습니다.");


    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, Integer code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
