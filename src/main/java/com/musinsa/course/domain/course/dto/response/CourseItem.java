package com.musinsa.course.domain.course.dto.response;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;

/**
 * 강좌 목록 응답 DTO.
 * (enrolled(현재 수강인원)는 D5에서 enrollment 집계로 추가 예정 — 지금은 제외)
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
            formatSchedule(course.getScheduleDay(), course.getScheduleStartMinute(), course.getScheduleEndMinute()),
            course.getDepartment().getId(),       // ← LAZY 로딩
            course.getDepartment().getName(),
            course.getProfessor().getId(),         // ← LAZY 로딩
            course.getProfessor().getName()
        );
    }

    /** enum + 분 단위(int) → "월 09:00-10:30" 형태의 표시용 문자열로 조립 */
    private static String formatSchedule(ScheduleDay day, int startMinute, int endMinute) {
        return day.getDescription() + " " + toHhmm(startMinute) + "-" + toHhmm(endMinute);
    }

    private static String toHhmm(int minuteOfDay) {
        return String.format("%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }
}
