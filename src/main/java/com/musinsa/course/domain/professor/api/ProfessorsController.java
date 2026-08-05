package com.musinsa.course.domain.professor.api;

import com.musinsa.course.domain.professor.dto.response.ProfessorItem;
import com.musinsa.course.domain.professor.service.ProfessorService;
import com.musinsa.course.global.api.response.ItemsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ProfessorsController {

    private final ProfessorService professorService;

    public ProfessorsController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping("/professors")
    public ResponseEntity<?> list(
        @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(200) int limit,
        @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        Page<ProfessorItem> page = professorService.findProfessors(limit, offset);
        return ResponseEntity.ok(new ItemsResponse<>(
            page.getContent(),
            new ItemsResponse.Page(limit, offset, (int) page.getTotalElements())
        ));
    }
}
