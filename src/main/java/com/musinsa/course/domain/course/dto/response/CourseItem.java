package com.musinsa.course.domain.course.dto.response;

import com.musinsa.course.domain.course.entity.Course;

/**
 * 강좌 목록 응답 DTO.
 * (enrolled(현재 수강인원)는 현재 미포함 — 필요 시 enrollment 집계로 추가)
 * 주의: department/professor는 @ManyToOne(LAZY)라 트랜잭션 안에서 from()을 호출해야 함.
 */
public record CourseItem(
    Long id,
    String name,
    int credits,
    int capacity,
    String schedule,
    Long departmentId,
    String departmentName,
    Long professorId,
    String professorName
) {
    public static CourseItem from(Course course) {
        return new CourseItem(
            course.getId(),
            course.getName(),
            course.getCredits(),
            course.getCapacity(),
            course.getScheduleText(),
            course.getDepartment().getId(),       // ← LAZY 로딩
            course.getDepartment().getName(),
            course.getProfessor().getId(),         // ← LAZY 로딩
            course.getProfessor().getName()
        );
    }
}
