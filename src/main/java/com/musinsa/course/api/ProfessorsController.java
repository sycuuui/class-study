package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfessorsController {
    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;
    private final InMemoryStore store;

    public ProfessorsController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/professors")
    public ItemsResponse<ProfessorItem> list(
        @RequestParam(name = "limit", required = false) Integer limitParam,
        @RequestParam(name = "offset", required = false) Integer offsetParam
    ) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        int offset = offsetParam == null ? DEFAULT_OFFSET : offsetParam;
        int total = store.getProfessors().size();
        List<ProfessorItem> items = store.getProfessors().stream()
            .skip(Math.max(0, offset))
            .limit(Math.max(0, limit))
            .map(ProfessorsController::toItem)
            .toList();
        return new ItemsResponse<>(items, new ItemsResponse.Page(limit, offset, total));
    }

    private static ProfessorItem toItem(SeedData.Professor professor) {
        return new ProfessorItem(
            professor.id(),
            professor.name(),
            professor.departmentId(),
            professor.departmentName()
        );
    }

    public record ProfessorItem(
        int id,
        String name,
        int departmentId,
        String departmentName
    ) {
    }
}
