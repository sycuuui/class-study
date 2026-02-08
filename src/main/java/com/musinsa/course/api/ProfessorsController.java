package com.musinsa.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.SeedData;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfessorsController {
    private final InMemoryStore store;

    public ProfessorsController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/professors")
    public ItemsResponse<ProfessorItem> list() {
        List<ProfessorItem> items = store.getProfessors().stream()
            .map(ProfessorsController::toItem)
            .toList();
        return new ItemsResponse<>(items);
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
