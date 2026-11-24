package com.musinsa.course.domain.enrollment.dto.response;

public record EnrollmentRequest(
        long studentId,
        long courseId) {
}