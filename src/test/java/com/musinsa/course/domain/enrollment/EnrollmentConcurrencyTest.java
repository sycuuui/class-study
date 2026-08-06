package com.musinsa.course.domain.enrollment;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.professor.entity.Professor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 비관적 락(PESSIMISTIC_WRITE)이 정원 초과를 막는지 검증하는 통합 테스트.
 * 정원보다 많은 학생이 동시에 같은 강좌를 신청해도 성공은 정확히 정원 수여야 한다.
 */
class EnrollmentConcurrencyTest extends EnrollmentTestSupport {

    @Test
    @DisplayName("정원 20인 강좌에 60명이 동시 신청하면 정확히 20명만 성공한다")
    void capacity_isNeverExceeded_underConcurrency() throws Exception {
        // given
        Department department = saveDepartment();
        Professor professor = saveProfessor(department);
        int capacity = 20;
        Course course = saveCourse("자료구조", 3, capacity,
            ScheduleDay.MONDAY, 540, 630, department, professor);
        long courseId = course.getId();

        int studentCount = 60;
        List<Long> studentIds = new ArrayList<>();
        for (int i = 0; i < studentCount; i++) {
            studentIds.add(saveStudent(18, department).getId());
        }

        // when: 모든 스레드를 동시에 출발시켜 신청
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(studentCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (Long studentId : studentIds) {
            pool.submit(() -> {
                try {
                    start.await();
                    enrollmentService.requestEnrollment(studentId, courseId);
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        // then
        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(capacity);
        assertThat(failure.get()).isEqualTo(studentCount - capacity);
        assertThat(enrollmentRepository.countByCourseAndDeletedAtIsNull(course)).isEqualTo(capacity);
    }
}
