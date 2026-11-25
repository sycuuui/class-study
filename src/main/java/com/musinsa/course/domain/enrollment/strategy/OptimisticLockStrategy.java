package com.musinsa.course.domain.enrollment.strategy;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.repository.CourseRepository;
import com.musinsa.course.domain.course.service.validator.CourseValidator;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "enrollment.strategy", havingValue = "optimistic")
public class OptimisticLockStrategy implements CapacityReservationStrategy{
    final CourseRepository courseRepository;
    final CourseValidator courseValidator;
    @Override
    public Course reserveSeat(long courseId) {
        //강좌 존재(낙관적 락 걸기)
        Course course =  courseRepository.findCourseWithOptimisticLock(courseId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COURSE_NOT_FOUND));

        //정원 초과 확인
        long countCurrent = course.getEnrolled();
        courseValidator.checkCapacity(course, countCurrent);

        course.increaseEnrolled();

        return course;
    }

    @Override
    public Course releaseSeat(long courseId) {
        Course course = courseRepository.findCourseWithOptimisticLock(courseId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COURSE_NOT_FOUND));

        course.decreaseEnrolled();
        return course;
    }
}
