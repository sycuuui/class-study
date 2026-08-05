package com.musinsa.course.domain.enrollment.repository;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.domain.student.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByCourseAndDeletedAtIsNull(Course course);
    boolean existsByStudentAndCourseAndDeletedAtIsNull(Student student, Course course);
    Optional<Enrollment> findByStudentAndCourseAndDeletedAtIsNull(Student student, Course course);

    /** 시간표 충돌 검사에서 각 enrollment.getCourse()를 만지므로 course를 함께 로딩 → 루프 N+1 방지. */
    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByStudentAndDeletedAtIsNull(Student student);
}
