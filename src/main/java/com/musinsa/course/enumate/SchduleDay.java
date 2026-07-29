package com.musinsa.course.enumate;

public enum SchduleDay {
    MONDAY("월"),
    TUESDAY("화"),
    WEDENSDAY("수"),
    THRSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    private final String description;

    SchduleDay(String description) {
        this.description = description;
    }
}
