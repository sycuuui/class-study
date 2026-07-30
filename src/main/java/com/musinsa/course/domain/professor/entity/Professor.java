package com.musinsa.course.domain.professor.entity;

import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "professor")
@Getter
public class Professor extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    protected Professor() {}

    @Builder
    public Professor(String name, Department department) {
        this.name = name;
        this.department = department;
    }
}
