package com.musinsa.course.api.response;

import java.util.Map;

public record ErrorResponse(Error error) {
    public record Error(int code, String message, Map<String, Object> details) {
    }
}
