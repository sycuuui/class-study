package com.musinsa.course.domain.enrollment.dto.response;

import com.musinsa.course.domain.course.entity.Course;

/**
 * 시간표 한 칸(수강 강좌) 응답 DTO.
 * 주의: professor는 @ManyToOne(LAZY)라 트랜잭션 안에서 from()을 호출해야 함.
 */
public record TimetableItem(
    Long id,
    String name,
    int credits,
    String schedule,
    Long professorId,
    String professorName
) {
    public static TimetableItem from(Course course) {
        return new TimetableItem(
            course.getId(),
            course.getName(),
            course.getCredits(),
            course.getScheduleText(),
            course.getProfessor().getId(),     // ← LAZY 로딩
            course.getProfessor().getName()
        );
    }
}
