package com.musinsa.course.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentsController {
    @GetMapping("/students")
    public ItemsResponse<StudentItem> list() {
        return new ItemsResponse<>(List.of(
            new StudentItem(1001, "김민준", 10, "컴퓨터공학과", 18, 12),
            new StudentItem(1002, "이서연", 11, "경영학과", 18, 15),
            new StudentItem(1003, "박지훈", 12, "전자공학과", 18, 9),
            new StudentItem(1004, "최유진", 13, "수학과", 18, 6),
            new StudentItem(1005, "정하늘", 14, "영문학과", 18, 3)
        ));
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
