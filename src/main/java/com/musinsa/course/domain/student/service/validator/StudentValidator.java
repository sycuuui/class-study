package com.musinsa.course.domain.student.service.validator;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StudentValidator {
    public void checkMaxCredits(Student student, int currentCredits, int courseCredits){
        if(student.getMaxCredits()<(currentCredits+courseCredits)){
            throw new RuntimeException("학생 최대 학점 초과입니다.");
        }
    }
}
