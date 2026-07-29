package com.musinsa.course.entity;

import com.musinsa.course.enumate.SchduleDay;
import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchduleDay schdule_day;

    @Column(nullable = false)
    private LocalDateTime schedule_start_min;

    @Column(nullable = false)
    private LocalDateTime schedule_start_max;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @Builder
    public Course(String name, int max_credits, int min_credits, int capacity){

    }
}
