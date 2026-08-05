package com.musinsa.course.domain.enrollment.service.validator;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentValidator {

    public void validateNotDuplicated(boolean exsits){
        if(exsits){
            throw new RuntimeException("중복 요청입니다.");
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
            throw new RuntimeException("시간표가 겹칩니다.");
        }
    }
}
