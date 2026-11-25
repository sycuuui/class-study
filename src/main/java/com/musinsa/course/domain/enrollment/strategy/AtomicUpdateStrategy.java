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
@ConditionalOnProperty(name = "enrollment.strategy", havingValue = "atomic")
public class AtomicUpdateStrategy implements CapacityReservationStrategy{
    final CourseRepository courseRepository;
    final CourseValidator courseValidator;

    @Override
    public Course reserveSeat(long courseId) {
        int state = courseRepository.updateCourseByCapacityIsLessThanEnrolled(courseId);
        Course course =  courseRepository.findCourseById(courseId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COURSE_NOT_FOUND));
        if(state==0){
            throw new ApplicationException(ErrorCode.CAPACITY_EXCEEDED);
        }
        return course;
    }

    @Override
    public Course releaseSeat(long courseId) {
        courseRepository.decreaseCourseEnrolled(courseId);

        return courseRepository.findCourseById(courseId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COURSE_NOT_FOUND));
    }
}
