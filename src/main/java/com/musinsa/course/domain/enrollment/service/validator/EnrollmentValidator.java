package com.musinsa.course.domain.enrollment.service.validator;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentValidator {

    public void validateNotDuplicated(boolean exists){
        if(exists){
            throw new ApplicationException(ErrorCode.DUPLICATE_ENROLLMENT);
        }
    }

    public void checkCourseTime(Enrollment enrollment, Course course){
        int courseStartTime = course.getScheduleStartMinute();
        int courseEndTime = course.getScheduleEndMinute();
        ScheduleDay scheduleDay = course.getScheduleDay();

        Course enrollmentCourse = enrollment.getCourse();
        if(scheduleDay!=enrollmentCourse.getScheduleDay()) return;
        if(enrollmentCourse.getScheduleEndMinute()>courseStartTime
                && enrollmentCourse.getScheduleStartMinute()<courseEndTime){
            throw new ApplicationException(ErrorCode.SCHEDULE_CONFLICT);
        }
    }
}
