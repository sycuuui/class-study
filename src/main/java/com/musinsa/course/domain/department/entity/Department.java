package com.musinsa.course.domain.department.entity;

import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "department")
@Getter
public class Department extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Department() {}

    @Builder
    public Department(String name) {
        this.name = name;
    }
}
