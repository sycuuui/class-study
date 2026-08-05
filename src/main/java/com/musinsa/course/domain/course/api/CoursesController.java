package com.musinsa.course.domain.course.api;

import com.musinsa.course.domain.course.dto.response.CourseItem;
import com.musinsa.course.domain.course.service.CourseService;
import com.musinsa.course.global.api.response.ItemsResponse;
import com.musinsa.course.global.common.ApplicationResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class CoursesController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public ApplicationResponse<ItemsResponse<CourseItem>> list(
        @RequestParam(name = "departmentId", required = false) Long departmentId,
        @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(200) int limit,
        @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        Page<CourseItem> page = courseService.findCourses(departmentId, limit, offset);
        ItemsResponse<CourseItem> items = new ItemsResponse<>(
            page.getContent(),
            new ItemsResponse.Page(limit, offset, (int) page.getTotalElements())
        );
        return ApplicationResponse.ok(items);
    }
}
