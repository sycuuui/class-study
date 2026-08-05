package com.musinsa.course.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationResponse<T>(
        LocalDateTime timestamp,
        Integer code,
        String message,
        T data
) {

    public static <T> ApplicationResponse<T> ok(T data) {
        return of(ErrorCode.SUCCESS, data);
    }

    public static <T> ApplicationResponse<T> ok() {
        return of(ErrorCode.SUCCESS, null);
    }

    public static <T> ApplicationResponse<T> created(T data) {
        return of(ErrorCode.CREATED, data);
    }

    public static <T> ApplicationResponse<T> created() {
        return of(ErrorCode.CREATED, null);
    }

    private static <T> ApplicationResponse<T> of(ErrorCode code, T data) {
        return ApplicationResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }
}
