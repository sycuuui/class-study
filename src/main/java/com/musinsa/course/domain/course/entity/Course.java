package com.musinsa.course.domain.course.entity;

import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.professor.entity.Professor;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "course")
@Getter
@SQLDelete(sql = "UPDATE course SET deleted_at = NOW() where id = ?")
public class Course extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "schedule_day", nullable = false)
    @Enumerated(EnumType.STRING)
    private ScheduleDay scheduleDay;

    @Column(name = "schedule_start_min", nullable = false)
    private int scheduleStartMinute;

    @Column(name = "schedule_end_min", nullable = false)
    private int scheduleEndMinute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    protected Course() {}

    @Builder
    public Course(String name, int credits, int capacity,
                  ScheduleDay scheduleDay, int scheduleStartMinute, int scheduleEndMinute,
                  Department department, Professor professor) {
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.scheduleDay = scheduleDay;
        this.scheduleStartMinute = scheduleStartMinute;
        this.scheduleEndMinute = scheduleEndMinute;
        this.department = department;
        this.professor = professor;
    }

    /** 표시용 스케줄 문자열. 예: "월 09:00-10:30" */
    public String getScheduleText() {
        return scheduleDay.getDescription() + " " + toHhmm(scheduleStartMinute) + "-" + toHhmm(scheduleEndMinute);
    }

    private static String toHhmm(int minuteOfDay) {
        return String.format("%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }
}
