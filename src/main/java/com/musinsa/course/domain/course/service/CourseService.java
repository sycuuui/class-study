package com.musinsa.course.domain.course.service;

import com.musinsa.course.domain.course.dto.response.CourseItem;
import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * 강좌 목록 페이징 조회. departmentId가 있으면 학과별 필터.
     * readOnly 트랜잭션 안에서 DTO 변환까지 끝내 LAZY(department/professor) 접근을 안전하게 처리.
     */
    @Transactional(readOnly = true)
    public Page<CourseItem> findCourses(Long departmentId, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Course> page = (departmentId == null)
            ? courseRepository.findAll(pageable)
            : courseRepository.findByDepartment_Id(departmentId, pageable);
        return page.map(CourseItem::from);
    }
}
