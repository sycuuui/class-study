package com.musinsa.course.domain.student.dto.response;

import com.musinsa.course.domain.student.entity.Student;
import lombok.Builder;

/**
 * 학생 목록 응답 DTO. (신청 학점은 필요 시 enrollment 집계로 계산 — 엔티티에 저장하지 않음)
 * 엔티티를 그대로 노출하지 않기 위한 외부 표현.
 */
@Builder
public record StudentItem(
    Long id,
    String name,
    Long departmentId,
    String departmentName,
    int maxCredits
) {
    /**
     * 엔티티 → DTO 변환.
     * 주의: department는 @ManyToOne(LAZY)이므로 이 메서드는 트랜잭션(영속성 컨텍스트) 안에서 호출해야 함.
     */
    public static StudentItem from(Student student) {
        return new StudentItem(student.getId(),
                student.getName(),
                student.getDepartment().getId(),
                student.getDepartment().getName(),
                student.getMaxCredits());
    }
}
