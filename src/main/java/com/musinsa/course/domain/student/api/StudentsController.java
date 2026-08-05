package com.musinsa.course.domain.student.api;

import com.musinsa.course.domain.student.dto.response.ItemResponse;
import com.musinsa.course.domain.student.dto.response.StudentItem;
import com.musinsa.course.domain.student.service.StudentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated   // 메서드 파라미터의 @Min/@Max 제약을 활성화 (위반 시 ConstraintViolationException → 400)
public class StudentsController {

    private final StudentService studentService;

    public StudentsController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/students")
    public ResponseEntity<?> list(
        @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(200) int limit,
        @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        Page<StudentItem> studentItems = studentService.findStudents(limit, offset);
        return ResponseEntity.ok(ItemResponse.of(studentItems));
    }
}
