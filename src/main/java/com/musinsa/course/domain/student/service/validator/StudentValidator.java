package com.musinsa.course.domain.student.service.validator;

import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentValidator {
    public void checkMaxCredits(Student student, int currentCredits, int courseCredits){
        if(student.getMaxCredits()<(currentCredits+courseCredits)){
            throw new ApplicationException(ErrorCode.CREDIT_LIMIT_EXCEEDED);
        }
    }
}
