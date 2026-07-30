package com.musinsa.course.domain.professor.repository;

import com.musinsa.course.domain.professor.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
