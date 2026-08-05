package com.musinsa.course.domain.professor.repository;

import com.musinsa.course.domain.professor.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    /** 교수 페이징 조회. @EntityGraph로 department 함께 로딩 → 목록 DTO 변환 시 N+1 방지. */
    @EntityGraph(attributePaths = {"department"})
    Page<Professor> findAll(Pageable pageable);
}
