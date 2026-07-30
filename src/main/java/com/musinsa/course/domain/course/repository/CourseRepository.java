package com.musinsa.course.domain.course.repository;

import com.musinsa.course.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
