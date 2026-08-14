package com.musinsa.course.domain.student.entity;

import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "student")
@Getter
@SQLDelete(sql = "UPDATE student SET deleted_at = NOW() where id = ?")
public class Student extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_credits", nullable = false)
    private int maxCredits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    protected Student() {}

    @Builder
    public Student(String name, int maxCredits, Department department) {
        this.name = name;
        this.maxCredits = maxCredits;
        this.department = department;
    }
}
