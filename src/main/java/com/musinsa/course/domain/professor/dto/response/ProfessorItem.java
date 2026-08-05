package com.musinsa.course.domain.professor.dto.response;

import com.musinsa.course.domain.professor.entity.Professor;

/**
 * 교수 목록 응답 DTO.
 * 주의: department는 @ManyToOne(LAZY)라 트랜잭션 안에서 from()을 호출해야 함.
 */
public record ProfessorItem(
    Long id,
    String name,
    Long departmentId,
    String departmentName
) {
    public static ProfessorItem from(Professor professor) {
        return new ProfessorItem(
            professor.getId(),
            professor.getName(),
            professor.getDepartment().getId(),     // ← LAZY 로딩
            professor.getDepartment().getName()
        );
    }
}
