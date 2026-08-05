package com.musinsa.course.global.api;

import com.musinsa.course.global.api.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 전역 예외 처리. 지금은 Bean Validation 실패(@Min/@Max 등)만 400으로 변환.
 * (D6에서 커스텀 예외·ErrorCode enum으로 확장 예정)
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** @RequestParam 등에 붙인 제약(@Min/@Max) 위반 → 400 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        ErrorResponse body = new ErrorResponse(
            new ErrorResponse.Error(400, "잘못된 요청 파라미터", Map.of("detail", e.getMessage())));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
