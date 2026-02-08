package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentsController {
    private final InMemoryStore store;

    public StudentsController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/students")
    public ItemsResponse<StudentItem> list() {
        List<StudentItem> items = store.getStudents().stream()
            .map(StudentsController::toItem)
            .toList();
        return new ItemsResponse<>(items);
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
