package com.musinsa.course.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryStore {
    private volatile boolean ready = false;
    private List<SeedData.Department> departments = Collections.emptyList();
    private List<SeedData.Professor> professors = Collections.emptyList();
    private List<CourseState> courses = Collections.emptyList();
    private List<SeedData.Student> students = Collections.emptyList();
    private Map<Integer, SeedData.Student> studentsById = Collections.emptyMap();
    private Map<Integer, CourseState> coursesById = Collections.emptyMap();

    public boolean isReady() {
        return ready;
    }

    public List<SeedData.Department> getDepartments() {
        return departments;
    }

    public List<SeedData.Professor> getProfessors() {
        return professors;
    }

    public List<SeedData.Course> getCourses() {
        return courses.stream()
            .map(CourseState::toCourse)
            .toList();
    }

    public List<SeedData.Student> getStudents() {
        return students;
    }

    void setData(SeedData data) {
        this.departments = data.departments();
        this.professors = data.professors();
        this.courses = data.courses().stream()
            .map(CourseState::fromCourse)
            .toList();
        this.students = data.students();
        this.studentsById = data.students().stream()
            .collect(java.util.stream.Collectors.toMap(SeedData.Student::id, s -> s));
        this.coursesById = this.courses.stream()
            .collect(java.util.stream.Collectors.toMap(cs -> cs.id, cs -> cs));
        this.ready = true;
    }

    private static final class CourseState {
        private final int id;
        private final String name;
        private final int credits;
        private final int capacity;
        private final String schedule;
        private final int departmentId;
        private final String departmentName;
        private final int professorId;
        private final String professorName;
        private volatile int enrolled;

        private CourseState(
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
            this.id = id;
            this.name = name;
            this.credits = credits;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.schedule = schedule;
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.professorId = professorId;
            this.professorName = professorName;
        }

        private static CourseState fromCourse(SeedData.Course course) {
            return new CourseState(
                course.id(),
                course.name(),
                course.credits(),
                course.capacity(),
                course.enrolled(),
                course.schedule(),
                course.departmentId(),
                course.departmentName(),
                course.professorId(),
                course.professorName()
            );
        }

        private SeedData.Course toCourse() {
            return new SeedData.Course(
                id,
                name,
                credits,
                capacity,
                enrolled,
                schedule,
                departmentId,
                departmentName,
                professorId,
                professorName
            );
        }
    }
}
