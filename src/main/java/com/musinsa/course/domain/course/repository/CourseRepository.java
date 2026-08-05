package com.musinsa.course.domain.course.repository;

import com.musinsa.course.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 전체 강좌 페이징 조회.
     * @EntityGraph로 department/professor를 함께 로딩(fetch join) → 목록 DTO 변환 시 N+1 방지.
     * (department/professor는 to-one이라 페이징과 함께 써도 안전)
     */
    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findAll(Pageable pageable);

    /** 학과별 강좌 페이징 조회 (departmentId 필터). N+1 방지를 위해 동일하게 EntityGraph 적용. */
    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findByDepartment_Id(Long departmentId, Pageable pageable);
}
