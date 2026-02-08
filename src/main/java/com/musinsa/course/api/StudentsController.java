package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
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
    public ItemsResponse<StudentItem> list(
        @RequestParam(name = "limit", required = false) Integer limitParam,
        @RequestParam(name = "offset", required = false) Integer offsetParam
    ) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        int offset = offsetParam == null ? DEFAULT_OFFSET : offsetParam;
        int total = store.getStudents().size();
        List<StudentItem> items = store.getStudents().stream()
            .skip(Math.max(0, offset))
            .limit(Math.max(0, limit))
            .map(StudentsController::toItem)
            .toList();
        return new ItemsResponse<>(items, new ItemsResponse.Page(limit, offset, total));
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
}
