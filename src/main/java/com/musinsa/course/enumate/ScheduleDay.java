package com.musinsa.course.enumate;

import lombok.Getter;

@Getter
public enum ScheduleDay {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    private final String description;

    ScheduleDay(String description) {
        this.description = description;
    }
}
