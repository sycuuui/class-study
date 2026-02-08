package com.musinsa.course.api.response;

import java.util.List;

public record ItemsResponse<T>(List<T> items, Page page) {
    public record Page(int limit, int offset, int total) {
    }
}
