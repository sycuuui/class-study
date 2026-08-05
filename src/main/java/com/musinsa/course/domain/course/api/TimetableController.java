package com.musinsa.course.domain.course.api;

import com.musinsa.course.data.InMemoryStore;
import com.musinsa.course.data.InMemoryStore.TimetableResult;
import com.musinsa.course.data.SeedData;
import com.musinsa.course.global.common.ApplicationResponse;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시간표 조회. 데이터 소스는 아직 InMemoryStore(추후 DB 이관 예정)이나,
 * 응답/예외 형식만 공통 구조(ApplicationResponse/ApplicationException)로 통일.
 */
@RestController
@RequiredArgsConstructor
public class TimetableController {
    private final InMemoryStore store;

    @GetMapping("/timetable")
    public ApplicationResponse<TimetableResponse> get(@RequestParam(name = "studentId") int studentId) {
        TimetableResult result = store.timetable(studentId);
        if (!result.success) {
            throw new ApplicationException(
                result.httpStatus == 404 ? ErrorCode.STUDENT_NOT_FOUND : ErrorCode.INVALID_VALUE_EXCEPTION);
        }
        List<TimetableItem> items = result.items.stream()
            .map(TimetableController::toItem)
            .toList();
        return ApplicationResponse.ok(new TimetableResponse(studentId, items, result.totalCredits));
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
}
