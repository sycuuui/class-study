package com.musinsa.course.data;

import java.util.List;

public record SeedData(
    List<Department> departments,
    List<Professor> professors,
    List<Course> courses,
    List<Student> students
) {
    public record Department(int id, String name) {
    }

    public record Professor(
        int id,
        String name,
        int departmentId,
        String departmentName
    ) {
    }

    public record Course(
        int id,
        String name,
        int credits,
        int capacity,
        int enrolled,
        String schedule,
        int departmentId,
        String departmentName,
        int professorId,
        String professorName
    ) {
    }

    public record Student(
        int id,
        String name,
        int departmentId,
        String departmentName,
        int maxCredits,
        int enrolledCredits
    ) {
    }
}
