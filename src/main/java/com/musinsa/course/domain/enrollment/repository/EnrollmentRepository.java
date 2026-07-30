package com.musinsa.course.domain.enrollment.repository;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByCourse(Course course);
    boolean existsByStudentAndCourse(Student student, Course course);
    List<Enrollment> findByStudent(Student student);
}
