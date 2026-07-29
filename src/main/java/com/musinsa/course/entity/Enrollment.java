package com.musinsa.course.entity;

import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_enrollment_student_course",
                        columnNames = {"student_id", "course_id"}
                )
        }
)
public class Enrollment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    protected Enrollment() {}

    @Builder
    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
    }
}
