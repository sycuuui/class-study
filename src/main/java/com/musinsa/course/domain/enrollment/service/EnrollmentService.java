package com.musinsa.course.domain.enrollment.service;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.repository.CourseRepository;
import com.musinsa.course.domain.course.service.validator.CourseValidator;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.domain.enrollment.repository.EnrollmentRepository;
import com.musinsa.course.domain.enrollment.service.validator.EnrollmentValidator;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.domain.student.repository.StudentRepository;
import com.musinsa.course.domain.student.service.validator.StudentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final StudentValidator studentValidator;
    private final CourseValidator courseValidator;
    private final EnrollmentValidator enrollmentValidator;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public void requestEnrollment(long studentId, long courseId){
        //학생 존재
        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 학생입니다."));
        //강좌 존재
        Course course = courseRepository.findCourseByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 강좌입니다."));

        //강좌 기간 확인(추후 필요 ex. 년도+학기)(이건 @Schduler로 일정 시간이 되면 학기별로 강좌 및 학생 기록 닫기)

        //중복 신청 여부
        boolean exsits = enrollmentRepository.existsByStudentAndCourseAndDeletedAtIsNull(student,course);
        enrollmentValidator.validateNotDuplicated(exsits);
        //정원 초과
        long countCurrent = enrollmentRepository.countByCourseAndDeletedAtIsNull(course);
        courseValidator.checkCapacity(course, countCurrent);

        //학생의 현재 수강 신청 리스트 가져오기(+추후 강좌 기간 도입 시, 기간 설정 => 탐색 수강 신청 데이터 줄어듬)
        List<Enrollment> enrollments = enrollmentRepository.findByStudentAndDeletedAtIsNull(student);

        int currentCredits=0;
        for(Enrollment enrollment: enrollments){
            //시간표 충돌
            enrollmentValidator.checkCourseTime(enrollment,course);
            currentCredits+=enrollment.getCourse().getCredits();
        }
        //학점 초과
        studentValidator.checkMaxCredits(student,currentCredits,course.getCredits());

        //----enrollment 반영----
        //강좌 넣기.
        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(student)
                .build();
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void cancel(long studentId, long courseId) {
        //학생 존재
        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 학생입니다."));
        //강좌 존재
        Course course = courseRepository.findCourseByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 강좌입니다."));

        //강좌 기간 확인(추후 필요 ex. 년도+학기)(이건 @Schduler로 일정 시간이 되면 학기별로 강좌 및 학생 기록 닫기)

        //수강 신청 존재
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourseAndDeletedAtIsNull(student,course)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 수강 신청입니다."));

        enrollmentRepository.delete(enrollment);
    }
}
