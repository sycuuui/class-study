package com.musinsa.course.domain.enrollment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.professor.entity.Professor;
import com.musinsa.course.domain.student.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** 컨트롤러 계층: 공통 응답(ApplicationResponse) 구조와 HTTP 상태/에러코드 검증. */
@AutoConfigureMockMvc
class EnrollmentApiTest extends EnrollmentTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("수강신청 성공 시 201 + code 1001 봉투")
    void enroll_created() throws Exception {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Student student = saveStudent(18, d);

        mockMvc.perform(post("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":%d,\"courseId\":%d}".formatted(student.getId(), course.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(1001))
            .andExpect(jsonPath("$.data.status").value("ENROLLED"));
    }

    @Test
    @DisplayName("없는 학생 신청 시 404 + code 5000")
    void enroll_studentNotFound() throws Exception {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);

        mockMvc.perform(post("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":999999,\"courseId\":%d}".formatted(course.getId())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("취소 성공 시 200 + code 1000 봉투")
    void cancel_ok() throws Exception {
        Department d = saveDepartment();
        Professor p = saveProfessor(d);
        Course course = saveCourse("과목", 3, 30, ScheduleDay.MONDAY, 540, 630, d, p);
        Student student = saveStudent(18, d);
        enrollmentService.requestEnrollment(student.getId(), course.getId());

        mockMvc.perform(delete("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":%d,\"courseId\":%d}".formatted(student.getId(), course.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    @DisplayName("파라미터 검증 위반(limit 초과) 시 400 + code 2002")
    void listStudents_validationFails() throws Exception {
        mockMvc.perform(get("/students").param("limit", "999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(2002));
    }
}
