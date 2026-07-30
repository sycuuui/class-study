package com.musinsa.course.global.api.response;

import java.util.List;

public record ItemsResponse<T>(List<T> items, Page page) {
    public record Page(int limit, int offset, int total) {
    }
}
