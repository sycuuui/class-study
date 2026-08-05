package com.musinsa.course.domain.enrollment.api;

import com.musinsa.course.domain.enrollment.dto.response.TimetableResponse;
import com.musinsa.course.domain.enrollment.service.TimetableService;
import com.musinsa.course.global.common.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/timetable")
    public ApplicationResponse<TimetableResponse> get(@RequestParam(name = "studentId") long studentId) {
        return ApplicationResponse.ok(timetableService.getTimetable(studentId));
    }
}
