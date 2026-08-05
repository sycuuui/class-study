package com.musinsa.course.domain.course.service.validator;

import com.musinsa.course.domain.course.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseValidator {
    public void checkCapacity(Course course, long countCurrent){
        if(course.getCapacity()<countCurrent+1){
            throw new RuntimeException("수강신청 인원 초과입니다.");
        }
    }
}
