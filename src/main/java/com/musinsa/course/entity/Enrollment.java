package com.musinsa.course.entity;

import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "request_id",
                        columnNames = {"student", "course"}
                )
        }
)
public class Enrollment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
}
