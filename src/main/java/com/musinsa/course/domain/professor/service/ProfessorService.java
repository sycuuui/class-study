package com.musinsa.course.domain.professor.service;

import com.musinsa.course.domain.professor.dto.response.ProfessorItem;
import com.musinsa.course.domain.professor.repository.ProfessorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProfessorItem> findProfessors(int limit, int offset) {
        return professorRepository.findAll(PageRequest.of(offset / limit, limit))
                                  .map(ProfessorItem::from);
    }
}
