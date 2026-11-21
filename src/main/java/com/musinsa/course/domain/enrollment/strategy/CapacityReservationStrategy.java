package com.musinsa.course.domain.enrollment.strategy;


import com.musinsa.course.domain.course.entity.Course;

//추후 동시성 제어 방법 4가지(비관적 락/낙관적 락/원자적 UPDATE/Redis) 각 성능 비교를 위해 인터페이스 구현
public interface CapacityReservationStrategy {
    long reserveSeat();
    Course releaseSeat();
}
