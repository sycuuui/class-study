package com.musinsa.course.api;

import com.musinsa.course.api.response.ErrorResponse;
import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.InMemoryStore.TimetableResult;
import com.musinsa.course.data.SeedData;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimetableController {
    private final InMemoryStore store;

    public TimetableController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/timetable")
    public ResponseEntity<?> get(@RequestParam(name = "studentId") int studentId) {
        TimetableResult result = store.timetable(studentId);
        if (!result.success) {
            return ResponseEntity.status(result.httpStatus).body(errorBody(result));
        }
        List<TimetableItem> items = result.items.stream()
            .map(TimetableController::toItem)
            .toList();
        return ResponseEntity.ok(new TimetableResponse(studentId, items, result.totalCredits));
    }

    public record TimetableResponse(int studentId, List<TimetableItem> items, int totalCredits) {
    }

    public record TimetableItem(
        int id,
        String name,
        int credits,
        String schedule,
        int professorId,
        String professorName
    ) {
    }

    private static TimetableItem toItem(SeedData.Course course) {
        return new TimetableItem(
            course.id(),
            course.name(),
            course.credits(),
            course.schedule(),
            course.professorId(),
            course.professorName()
        );
    }

    private static ErrorResponse errorBody(TimetableResult result) {
        Map<String, Object> details = Map.of(
            "studentId", result.studentId
        );
        return new ErrorResponse(new ErrorResponse.Error(result.errorCode, result.message, details));
    }
}
