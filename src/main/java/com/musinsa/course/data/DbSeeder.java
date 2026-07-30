package com.musinsa.course.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.musinsa.course.domain.course.entity.Course;
import com.musinsa.course.domain.course.enumate.ScheduleDay;
import com.musinsa.course.domain.course.repository.CourseRepository;
import com.musinsa.course.domain.department.entity.Department;
import com.musinsa.course.domain.department.repository.DepartmentRepository;
import com.musinsa.course.domain.professor.entity.Professor;
import com.musinsa.course.domain.professor.repository.ProfessorRepository;
import com.musinsa.course.domain.student.entity.Student;
import com.musinsa.course.domain.student.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인메모리로 생성된 시드 데이터(store)를 H2 DB로 옮긴다.
 * SeedDataGenerator(@Order(1))가 store를 채운 뒤 실행(@Order(2)).
 */
@Component
@Order(2)
public class DbSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DbSeeder.class);

    private final InMemoryStore store;
    private final DepartmentRepository departmentRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public DbSeeder(
        InMemoryStore store,
        DepartmentRepository departmentRepository,
        ProfessorRepository professorRepository,
        StudentRepository studentRepository,
        CourseRepository courseRepository
    ) {
        this.store = store;
        this.departmentRepository = departmentRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 파일 모드는 재시작해도 데이터가 남으므로, 이미 시드됐으면 중복 방지
        if (departmentRepository.count() > 0) {
            log.info("db seed skipped (already seeded)");
            return;
        }

        long start = System.currentTimeMillis();

        // 1) Department: 옛 record id → 저장된 엔티티 매핑
        Map<Integer, Department> deptMap = new HashMap<>();
        for (SeedData.Department r : store.getDepartments()) {
            Department saved = departmentRepository.save(
                Department.builder().name(r.name()).build());
            deptMap.put(r.id(), saved);
        }
        log.info("db seed departments={}", deptMap.size());

        // 2) Professor: dept 연결 + 옛 id → 엔티티 매핑
        Map<Integer, Professor> profMap = new HashMap<>();
        for (SeedData.Professor r : store.getProfessors()) {
            Professor saved = professorRepository.save(
                Professor.builder()
                    .name(r.name())
                    .department(deptMap.get(r.departmentId()))
                    .build());
            profMap.put(r.id(), saved);
        }
        log.info("db seed professors={}", profMap.size());

        // 3) Student: dept 연결 (enrolledCredits는 버림), saveAll 배치
        List<Student> students = new ArrayList<>(store.getStudents().size());
        for (SeedData.Student r : store.getStudents()) {
            students.add(
                Student.builder()
                    .name(r.name())
                    .maxCredits(r.maxCredits())
                    .department(deptMap.get(r.departmentId()))
                    .build());
        }
        studentRepository.saveAll(students);
        log.info("db seed students={}", students.size());

        // 4) Course: dept + prof 연결, schedule 문자열 파싱 (enrolled는 버림)
        List<Course> courses = new ArrayList<>(store.getCourses().size());
        for (SeedData.Course r : store.getCourses()) {
            String[] schedule = parseSchedule(r.schedule());
            courses.add(
                Course.builder()
                    .name(r.name())
                    .credits(r.credits())
                    .capacity(r.capacity())
                    .scheduleDay(parseDay(schedule[0]))
                    .scheduleStartMinute(toMinute(schedule[1]))
                    .scheduleEndMinute(toMinute(schedule[2]))
                    .department(deptMap.get(r.departmentId()))
                    .professor(profMap.get(r.professorId()))
                    .build());
        }
        courseRepository.saveAll(courses);
        log.info("db seed courses={}", courses.size());

        log.info("db seed total ms={}", (System.currentTimeMillis() - start));
    }

    /** "월 09:00-10:30" → ["월", "09:00", "10:30"] */
    private String[] parseSchedule(String schedule) {
        String[] dayAndTime = schedule.split(" ");   // ["월", "09:00-10:30"]
        String[] startEnd = dayAndTime[1].split("-"); // ["09:00", "10:30"]
        return new String[] {dayAndTime[0], startEnd[0], startEnd[1]};
    }

    private ScheduleDay parseDay(String day) {
        return switch (day) {
            case "월" -> ScheduleDay.MONDAY;
            case "화" -> ScheduleDay.TUESDAY;
            case "수" -> ScheduleDay.WEDNESDAY;
            case "목" -> ScheduleDay.THURSDAY;
            case "금" -> ScheduleDay.FRIDAY;
            case "토" -> ScheduleDay.SATURDAY;
            case "일" -> ScheduleDay.SUNDAY;
            default -> throw new IllegalArgumentException("unknown day: " + day);
        };
    }

    /** "09:00" → 540 (하루 중 분 단위) */
    private int toMinute(String hhmm) {
        String[] parts = hhmm.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }
}
