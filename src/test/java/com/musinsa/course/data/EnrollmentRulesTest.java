package com.musinsa.course.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

public class EnrollmentRulesTest {
    @Test
    void creditLimitExceeded_returns601() {
        InMemoryStore store = new InMemoryStore();

        List<SeedData.Department> departments = List.of(new SeedData.Department(10, "컴퓨터공학과"));
        List<SeedData.Professor> professors = List.of(new SeedData.Professor(500, "박지훈", 10, "컴퓨터공학과"));
        List<SeedData.Course> courses = List.of(
            new SeedData.Course(1, "과목1", 3, 30, 0, "월 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(2, "과목2", 3, 30, 0, "화 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(3, "과목3", 3, 30, 0, "수 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(4, "과목4", 3, 30, 0, "목 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(5, "과목5", 3, 30, 0, "금 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(6, "과목6", 3, 30, 0, "월 10:30-12:00", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(7, "과목7", 3, 30, 0, "화 10:30-12:00", 10, "컴퓨터공학과", 500, "박지훈")
        );
        List<SeedData.Student> students = List.of(
            new SeedData.Student(1000, "김민준", 10, "컴퓨터공학과", 18, 0)
        );
        store.setData(new SeedData(departments, professors, courses, students));

        for (int courseId = 1; courseId <= 6; courseId++) {
            store.enroll(1000, courseId);
        }

        InMemoryStore.EnrollmentResult result = store.enroll(1000, 7);
        assertEquals(601, result.errorCode);
        assertEquals(409, result.httpStatus);
    }

    @Test
    void timeConflict_returns602() {
        InMemoryStore store = new InMemoryStore();

        List<SeedData.Department> departments = List.of(new SeedData.Department(10, "컴퓨터공학과"));
        List<SeedData.Professor> professors = List.of(new SeedData.Professor(500, "박지훈", 10, "컴퓨터공학과"));
        List<SeedData.Course> courses = List.of(
            new SeedData.Course(1, "과목1", 3, 30, 0, "월 09:00-10:30", 10, "컴퓨터공학과", 500, "박지훈"),
            new SeedData.Course(2, "과목2", 3, 30, 0, "월 10:00-11:00", 10, "컴퓨터공학과", 500, "박지훈")
        );
        List<SeedData.Student> students = List.of(
            new SeedData.Student(1000, "김민준", 10, "컴퓨터공학과", 18, 0)
        );
        store.setData(new SeedData(departments, professors, courses, students));

        store.enroll(1000, 1);
        InMemoryStore.EnrollmentResult result = store.enroll(1000, 2);
        assertEquals(602, result.errorCode);
        assertEquals(409, result.httpStatus);
    }
}
