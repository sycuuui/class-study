package com.musinsa.course.domain.department.repository;

import com.musinsa.course.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
