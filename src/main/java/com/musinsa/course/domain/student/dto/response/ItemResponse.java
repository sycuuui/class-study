package com.musinsa.course.domain.student.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record ItemResponse(
        List<StudentItem> studentItemList,
        int pageNum,
        long total,
        int totalPages
) {
    public static ItemResponse of(Page<StudentItem> studentItemPage) {
        return ItemResponse.builder()
                .studentItemList(studentItemPage.getContent())
                .total(studentItemPage.getTotalElements())
                .totalPages(studentItemPage.getTotalPages())
                .pageNum(studentItemPage.getNumber())
                .build();
    }
}
