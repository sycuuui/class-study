package com.musinsa.course.api;

import java.util.List;

public record ItemsResponse<T>(List<T> items) {
}
