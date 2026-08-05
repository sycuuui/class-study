package com.musinsa.course.domain.enrollment.api;

import com.musinsa.course.domain.enrollment.service.EnrollmentService;
import com.musinsa.course.global.common.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentsController {

    private final EnrollmentService enrollmentService;

    /** 수강신청. 성공 시 201. 실패는 ApplicationException → GlobalExceptionHandler가 ErrorCode별 상태로 매핑. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        enrollmentService.requestEnrollment(request.studentId(), request.courseId());
        return ApplicationResponse.created(
            new EnrollmentResponse(request.studentId(), request.courseId(), "ENROLLED"));
    }

    /** 수강취소. 성공 시 200(빈 데이터 봉투). */
    @DeleteMapping
    public ApplicationResponse<Void> cancel(@RequestBody EnrollmentRequest request) {
        enrollmentService.cancel(request.studentId(), request.courseId());
        return ApplicationResponse.ok();
    }

    public record EnrollmentRequest(long studentId, long courseId) {
    }

    public record EnrollmentResponse(long studentId, long courseId, String status) {
    }
}
