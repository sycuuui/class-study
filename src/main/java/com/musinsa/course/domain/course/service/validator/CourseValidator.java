package com.musinsa.course.domain.course.service.validator;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseValidator {
    public void checkCapacity(Course course, long countCurrent){
        if(course.getCapacity()<countCurrent+1){
            throw new ApplicationException(ErrorCode.CAPACITY_EXCEEDED);
        }
    }
}
