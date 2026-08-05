package com.musinsa.course.domain.student.repository;

import com.musinsa.course.domain.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Page<Student> findByDeletedAtIsNull(Pageable pageable);

    Optional<Student> findByIdAndDeletedAtIsNull(Long id);
}
