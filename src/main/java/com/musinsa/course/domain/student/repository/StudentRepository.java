package com.musinsa.course.domain.student.repository;

import com.musinsa.course.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
