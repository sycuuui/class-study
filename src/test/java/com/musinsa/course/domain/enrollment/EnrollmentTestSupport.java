package com.musinsa.course.domain.enrollment;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.course.repository.CourseRepository;
import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.department.repository.DepartmentRepository;
import com.musinsa.course.domain.enrollment.repository.EnrollmentRepository;
import com.musinsa.course.domain.enrollment.service.EnrollmentService;
import com.musinsa.course.domain.professor.entity.Professor;
import com.musinsa.course.domain.professor.repository.ProfessorRepository;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.domain.student.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 수강신청 관련 통합 테스트 공통 지원.
 * - @ActiveProfiles("test"): 대량 시더(@Profile("!test"))를 끄고, 테스트가 필요한 데이터만 준비
 * - 테스트에 @Transactional을 붙이지 않는다: 별도 스레드/트랜잭션에서 데이터가 보여야 하고
 *   비관적 락도 트랜잭션 커밋 기준으로 동작하기 때문. 정리는 @AfterEach에서 직접 삭제.
 * - MySQL(H2 대신 운영과 동일한 DB)을 Testcontainers로 띄워 테스트. MYSQL 컨테이너를 static으로 둬서
 *   이 클래스를 상속하는 모든 테스트 클래스(EnrollmentServiceTest 등)가 컨테이너 1개를 공유 —
 *   그래야 Spring의 @SpringBootTest ApplicationContext 캐싱도 그대로 유지돼 테스트가 느려지지 않음.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
abstract class EnrollmentTestSupport {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("course_enroll_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired protected EnrollmentService enrollmentService;
    @Autowired protected DepartmentRepository departmentRepository;
    @Autowired protected ProfessorRepository professorRepository;
    @Autowired protected CourseRepository courseRepository;
    @Autowired protected StudentRepository studentRepository;
    @Autowired protected EnrollmentRepository enrollmentRepository;

    @AfterEach
    void cleanUp() {
        // FK 역순 삭제
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    protected Department saveDepartment() {
        return departmentRepository.save(Department.builder().name("컴퓨터공학과").build());
    }

    protected Professor saveProfessor(Department department) {
        return professorRepository.save(Professor.builder().name("박지훈").department(department).build());
    }

    protected Student saveStudent(int maxCredits, Department department) {
        return studentRepository.save(
            Student.builder().name("학생").maxCredits(maxCredits).department(department).build());
    }

    protected Course saveCourse(String name, int credits, int capacity,
                                ScheduleDay day, int startMinute, int endMinute,
                                Department department, Professor professor) {
        return courseRepository.save(Course.builder()
            .name(name).credits(credits).capacity(capacity)
            .scheduleDay(day).scheduleStartMinute(startMinute).scheduleEndMinute(endMinute)
            .department(department).professor(professor)
            .build());
    }
}
