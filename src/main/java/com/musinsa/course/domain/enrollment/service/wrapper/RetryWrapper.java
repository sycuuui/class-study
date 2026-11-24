package com.musinsa.course.domain.enrollment.service.wrapper;

import com.musinsa.course.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetryWrapper {
    final EnrollmentService enrollmentService;

    @Retryable(
            value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, random = true) //재시도 사이의 지연
    ) //ObjectOptimisticLockingFailureException 발생 시 지연, 영구적 실패까지 20번 재시도
    public void requestEnrollmentWithRetry(long studentId, long courseId){
        enrollmentService.requestEnrollment(studentId, courseId);
    }

    @Retryable(
            value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, random = true) //재시도 사이의 지연
    ) //ObjectOptimisticLockingFailureException 발생 시 지연, 영구적 실패까지 20번 재시도
    public void cancelEnrollmentWithRetry(long studentId, long courseId){
        enrollmentService.cancel(studentId, courseId);
    }
}
