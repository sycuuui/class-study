package com.musinsa.course.data;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InMemoryStore {
    private volatile boolean ready = false;
    private List<SeedData.Department> departments = Collections.emptyList();
    private List<SeedData.Professor> professors = Collections.emptyList();
    private List<SeedData.Course> courses = Collections.emptyList();
    private List<SeedData.Student> students = Collections.emptyList();

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
        return courses;
    }

    public List<SeedData.Student> getStudents() {
        return students;
    }

    void setData(SeedData data) {
        this.departments = data.departments();
        this.professors = data.professors();
        this.courses = data.courses();
        this.students = data.students();
        this.ready = true;
    }
}
