package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoursesController {
    private final InMemoryStore store;

    public CoursesController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/courses")
    public ItemsResponse<CourseItem> list(
        @RequestParam(name = "departmentId", required = false) Integer departmentId
    ) {
        List<CourseItem> items = store.getCourses().stream()
            .filter(course -> departmentId == null || course.departmentId() == departmentId)
            .map(CoursesController::toItem)
            .toList();
        return new ItemsResponse<>(items);
    }

    private static CourseItem toItem(SeedData.Course course) {
        return new CourseItem(
            course.id(),
            course.name(),
            course.credits(),
            course.capacity(),
            course.enrolled(),
            course.schedule(),
            course.departmentId(),
            course.departmentName(),
            course.professorId(),
            course.professorName()
        );
    }

    public record CourseItem(
        int id,
        String name,
        int credits,
        int capacity,
        int enrolled,
        String schedule,
        int departmentId,
        String departmentName,
        int professorId,
        String professorName
    ) {
    }
}
