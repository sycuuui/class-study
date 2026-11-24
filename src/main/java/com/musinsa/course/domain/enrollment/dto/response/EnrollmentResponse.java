package com.musinsa.course.domain.enrollment.dto.response;

public record EnrollmentResponse(
        long studentId,
        long courseId,
        String status) {
}
