package com.musinsa.course.api;

import java.util.Map;

public record ErrorResponse(Error error) {
    public record Error(int code, String message, Map<String, Object> details) {
    }
}
