package com.musinsa.course.domain.enrollment.strategy;

import com.musinsa.course.domain.course.entity.Course;

public class PessimisticLockStrategy implements CapacityReservationStrategy{

    @Override
    public long reserveSeat() {
        return 0;
    }

    @Override
    public Course releaseSeat() {
        return null;
    }
}
