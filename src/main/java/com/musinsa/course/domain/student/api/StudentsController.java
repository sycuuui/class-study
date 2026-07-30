package com.musinsa.course.domain.student.api;

import com.musinsa.course.global.api.response.ErrorResponse;
import com.musinsa.course.global.api.response.ItemsResponse;
import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentsController {
    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;
    private final InMemoryStore store;

    public StudentsController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/students")
    public ResponseEntity<?> list(
        @RequestParam(name = "limit", required = false) Integer limitParam,
        @RequestParam(name = "offset", required = false) Integer offsetParam
    ) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        int offset = offsetParam == null ? DEFAULT_OFFSET : offsetParam;
        if (limit < 1 || limit > 200 || offset < 0) {
            return ResponseEntity.badRequest().body(invalidRequest());
        }
        int total = store.getStudents().size();
        List<StudentItem> items = store.getStudents().stream()
            .skip(offset)
            .limit(limit)
            .map(StudentsController::toItem)
            .toList();
        return ResponseEntity.ok(new ItemsResponse<>(items, new ItemsResponse.Page(limit, offset, total)));
    }

    private static StudentItem toItem(SeedData.Student student) {
        return new StudentItem(
            student.id(),
            student.name(),
            student.departmentId(),
            student.departmentName(),
            student.maxCredits(),
            student.enrolledCredits()
        );
    }

    public record StudentItem(
        int id,
        String name,
        int departmentId,
        String departmentName,
        int maxCredits,
        int enrolledCredits
    ) {
    }

    private static ErrorResponse invalidRequest() {
        return new ErrorResponse(new ErrorResponse.Error(500, "잘못된 요청 파라미터", Map.of()));
    }
}
