package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.InMemoryStore.EnrollmentResult;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentsController {
    private final InMemoryStore store;

    private final String ENROLLED = "enrolled";

    public EnrollmentsController(InMemoryStore store) {
        this.store = store;
    }

    @PostMapping("/enrollments")
    public ResponseEntity<?> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentResult result = store.enroll(request.studentId(), request.courseId());
        if (result.success) {
            return ResponseEntity.status(201).body(new EnrollmentResponse(
                result.enrollmentId,
                result.studentId,
                result.courseId,
                    ENROLLED
            ));
        }
        return ResponseEntity.status(result.httpStatus).body(errorBody(result));
    }

    @DeleteMapping("/enrollments")
    public ResponseEntity<?> cancel(@RequestBody EnrollmentRequest request) {
        EnrollmentResult result = store.cancel(request.studentId(), request.courseId());
        if (result.canceled) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(result.httpStatus).body(errorBody(result));
    }

    private static ErrorResponse errorBody(EnrollmentResult result) {
        Map<String, Object> details = Map.of(
            "studentId", result.studentId,
            "courseId", result.courseId
        );
        return new ErrorResponse(new ErrorResponse.Error(result.errorCode, result.message, details));
    }

    public record EnrollmentRequest(int studentId, int courseId) {
    }

    public record EnrollmentResponse(long enrollmentId, int studentId, int courseId, String status) {
    }
}
