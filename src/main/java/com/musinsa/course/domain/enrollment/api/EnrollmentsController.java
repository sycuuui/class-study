package com.musinsa.course.domain.enrollment.api;

import com.musinsa.course.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentsController {

    private final EnrollmentService enrollmentService;

    /** 수강신청. 성공 시 201. (정원초과·중복 등 실패의 세밀한 HTTP 매핑은 D6에서 @RestControllerAdvice로) */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        enrollmentService.requestEnrollment(request.studentId(), request.courseId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new EnrollmentResponse(request.studentId(), request.courseId(), "ENROLLED"));
    }

    /** 수강취소. 성공 시 204. */
    @DeleteMapping
    public ResponseEntity<Void> cancel(@RequestBody EnrollmentRequest request) {
        enrollmentService.cancel(request.studentId(), request.courseId());
        return ResponseEntity.noContent().build();
    }

    public record EnrollmentRequest(long studentId, long courseId) {
    }

    public record EnrollmentResponse(long studentId, long courseId, String status) {
    }
}
