package com.musinsa.course.entity;

import com.musinsa.course.enumate.SchduleDay;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int max_credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchduleDay schdule_day;

    @Column(nullable = false)
    private LocalDate schedule_start_min;

    @Column(nullable = false)
    private LocalDate schedule_start_max;

    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Professor professor;

    @Builder
    public Course(String name, int max_credits, int min_credits, int capacity){

    }
}
