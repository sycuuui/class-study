package com.musinsa.course.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class EnrollmentConcurrencyTest {
    @Test
    void capacityOneCourse_allowsOnlyOneEnrollment() throws Exception {
        InMemoryStore store = new InMemoryStore();

        List<SeedData.Department> departments = List.of(new SeedData.Department(10, "컴퓨터공학과"));
        List<SeedData.Professor> professors = List.of(new SeedData.Professor(500, "박지훈", 10, "컴퓨터공학과"));
        List<SeedData.Course> courses = List.of(
            new SeedData.Course(1, "자료구조", 3, 1, 0, "월 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈")
        );
        List<SeedData.Student> students = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            students.add(new SeedData.Student(1000 + i, "김민준", 10, "컴퓨터공학과", 18, 0));
        }
        store.setData(new SeedData(departments, professors, courses, students));

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int studentId = 1000 + i;
            executor.submit(() -> {
                try {
                    start.await();
                    InMemoryStore.EnrollmentResult result = store.enroll(studentId, 1);
                    if (result.success) {
                        success.incrementAndGet();
                    } else {
                        failure.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertEquals(1, success.get());
        assertEquals(99, failure.get());
        int enrolled = store.getCourses().stream()
            .filter(course -> course.id() == 1)
            .findFirst()
            .orElseThrow()
            .enrolled();
        assertEquals(1, enrolled);
    }
}
