package com.musinsa.course.domain.student.service;

import com.musinsa.course.domain.student.dto.response.StudentItem;
import com.musinsa.course.domain.student.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * 학생 목록 페이징 조회.
     * readOnly 트랜잭션 안에서 DTO 변환까지 끝내야 LAZY(department) 접근이 안전하다.
     */
    @Transactional(readOnly = true)
    public Page<StudentItem> findStudents(int limit, int offset) {
        return studentRepository.findByDeletedAtIsNull(PageRequest.of(offset/limit,limit))
                        .map(StudentItem::from);
    }
}
