package com.musinsa.course.domain.enrollment.repository;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.domain.student.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByCourse(Course course);
    boolean existsByStudentAndCourse(Student student, Course course);
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

    /**
     * 학생의 수강 내역 조회. 시간표 충돌 검사·시간표 조회에서 course와 그 professor까지 만지므로
     * 중첩 경로로 함께 로딩 → N+1 방지.
     */
    @EntityGraph(attributePaths = {"course", "course.professor"})
    List<Enrollment> findByStudent(Student student);
}
