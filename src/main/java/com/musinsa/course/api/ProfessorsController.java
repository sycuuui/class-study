package com.musinsa.course.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfessorsController {
    @GetMapping("/professors")
    public ItemsResponse<ProfessorItem> list() {
        return new ItemsResponse<>(List.of(
            new ProfessorItem(501, "박지훈", 10, "컴퓨터공학과"),
            new ProfessorItem(502, "김수진", 11, "경영학과"),
            new ProfessorItem(503, "이도현", 12, "전자공학과"),
            new ProfessorItem(504, "최민아", 13, "수학과"),
            new ProfessorItem(505, "정태윤", 14, "영문학과")
        ));
    }

    public record ProfessorItem(
        int id,
        String name,
        int departmentId,
        String departmentName
    ) {
    }
}
