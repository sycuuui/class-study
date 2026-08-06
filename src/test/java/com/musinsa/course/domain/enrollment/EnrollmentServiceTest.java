package com.musinsa.course.domain.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.professor.entity.Professor;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 수강신청/취소 비즈니스 규칙이 올바른 ErrorCode로 실패하는지 검증. */
class EnrollmentServiceTest extends EnrollmentTestSupport {

    @Test
    @DisplayName("존재하지 않는 학생이면 STUDENT_NOT_FOUND")
    void studentNotFound() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(999_999L, course.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 강좌면 COURSE_NOT_FOUND")
    void courseNotFound() {
        Department d = saveDepartment();
        Student student = saveStudent(18, d);

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(student.getId(), 999_999L))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("같은 강좌를 두 번 신청하면 DUPLICATE_ENROLLMENT")
    void duplicateEnrollment() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Student student = saveStudent(18, d);

        enrollmentService.requestEnrollment(student.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(student.getId(), course.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_ENROLLMENT);
    }

    @Test
    @DisplayName("정원이 꽉 차면 CAPACITY_EXCEEDED")
    void capacityExceeded() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 1, ScheduleDay.MONDAY, 540, 630, d, p); // 정원 1
        Student first = saveStudent(18, d);
        Student second = saveStudent(18, d);

        enrollmentService.requestEnrollment(first.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(second.getId(), course.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.CAPACITY_EXCEEDED);
    }

    @Test
    @DisplayName("같은 요일 시간이 겹치면 SCHEDULE_CONFLICT")
    void scheduleConflict() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course monday9 = saveCourse("월9시", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p); // 09:00~10:30
        Course monday10 = saveCourse("월10시", 3, 30, ScheduleDay.MONDAY, 600, 700, d, p); // 10:00~11:40 (겹침)
        Student student = saveStudent(18, d);

        enrollmentService.requestEnrollment(student.getId(), monday9.getId());

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(student.getId(), monday10.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.SCHEDULE_CONFLICT);
    }

    @Test
    @DisplayName("최대 학점을 넘으면 CREDIT_LIMIT_EXCEEDED")
    void creditLimitExceeded() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course c1 = saveCourse("월", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Course c2 = saveCourse("화", 3, 30, ScheduleDay.TUESDAY, 540, 630, d, p);
        Student student = saveStudent(3, d); // 최대 3학점

        enrollmentService.requestEnrollment(student.getId(), c1.getId()); // 3학점 (딱 참)

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(student.getId(), c2.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.CREDIT_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("신청 후 취소하면 수강 내역이 사라지고 재신청이 가능하다")
    void cancelThenReEnroll() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Student student = saveStudent(18, d);

        enrollmentService.requestEnrollment(student.getId(), course.getId());
        enrollmentService.cancel(student.getId(), course.getId());

        assertThat(enrollmentRepository.countByCourseAndDeletedAtIsNull(course)).isZero();
        // 취소 후 재신청은 예외 없이 성공
        assertThatCode(() -> enrollmentService.requestEnrollment(student.getId(), course.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("신청하지 않은 강좌를 취소하면 ENROLLMENT_NOT_FOUND")
    void cancelNotFound() {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Student student = saveStudent(18, d);

        assertThatThrownBy(() -> enrollmentService.cancel(student.getId(), course.getId()))
            .isInstanceOf(ApplicationException.class)
            .extracting(e -> ((ApplicationException) e).getErrorCode())
            .isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
    }
}
