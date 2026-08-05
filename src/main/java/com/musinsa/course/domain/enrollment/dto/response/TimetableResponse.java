package com.musinsa.course.domain.enrollment.dto.response;

import java.util.List;

/** 학생 시간표 응답 DTO. */
public record TimetableResponse(
    Long studentId,
    List<TimetableItem> items,
    int totalCredits
) {
}
