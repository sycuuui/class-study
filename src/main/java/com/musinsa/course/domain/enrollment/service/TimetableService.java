package com.musinsa.course.domain.enrollment.service;

import com.musinsa.course.domain.enrollment.dto.response.TimetableItem;
import com.musinsa.course.domain.enrollment.dto.response.TimetableResponse;
import com.musinsa.course.domain.enrollment.entity.Enrollment;
import com.musinsa.course.domain.enrollment.repository.EnrollmentRepository;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.domain.student.repository.StudentRepository;
import com.musinsa.course.global.exception.ApplicationException;
import com.musinsa.course.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * 학생의 시간표 조회.
     * readOnly 트랜잭션 안에서 DTO 변환까지 끝내 LAZY(course/professor) 접근을 안전하게 처리.
     */
    @Transactional(readOnly = true)
    public TimetableResponse getTimetable(long studentId) {
        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STUDENT_NOT_FOUND));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentAndDeletedAtIsNull(student);

        List<TimetableItem> items = enrollments.stream()
                .map(enrollment -> TimetableItem.from(enrollment.getCourse()))
                .toList();
        int totalCredits = items.stream()
                .mapToInt(TimetableItem::credits)
                .sum();

        return new TimetableResponse(studentId, items, totalCredits);
    }
}
