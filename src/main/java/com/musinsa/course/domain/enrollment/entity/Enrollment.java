package com.musinsa.course.domain.enrollment.entity;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Builder
    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
    }
}
