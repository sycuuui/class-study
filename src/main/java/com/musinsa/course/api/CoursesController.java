package com.musinsa.course.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoursesController {
    @GetMapping("/courses")
    public ItemsResponse<CourseItem> list() {
        return new ItemsResponse<>(List.of(
            new CourseItem(1, "자료구조", 3, 30, 25, "월 09:00-10:30", 10, "컴퓨터공학과", 501, "박지훈"),
            new CourseItem(2, "운영체제", 3, 40, 32, "화 10:30-12:00", 10, "컴퓨터공학과", 501, "박지훈"),
            new CourseItem(3, "회계원리", 3, 35, 20, "수 09:00-10:30", 11, "경영학과", 502, "김수진"),
            new CourseItem(4, "전자회로", 3, 30, 28, "목 13:00-14:30", 12, "전자공학과", 503, "이도현"),
            new CourseItem(5, "선형대수", 3, 45, 38, "금 10:30-12:00", 13, "수학과", 504, "최민아")
        ));
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
