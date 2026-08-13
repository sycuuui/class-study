package com.musinsa.course.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(1)
@Profile("!test")   // 테스트 프로파일에서는 대량 시드를 돌리지 않는다 (테스트는 필요한 데이터만 직접 준비)
public class SeedDataGenerator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SeedDataGenerator.class);
    private static final int DEPARTMENT_MIN = 10;
    private static final int PROFESSOR_MIN = 100;
    private static final int COURSE_MIN = 500;
    private static final int STUDENT_MIN = 10_000;
    private static final long SEED = 42L;
    private static final int MAX_CREDITS = 18;

    private static final String[] DEPARTMENT_NAMES = {
        "컴퓨터공학과", "경영학과", "전자공학과", "수학과", "영문학과",
        "경제학과", "물리학과", "화학과", "기계공학과", "심리학과",
        "사회학과", "통계학과"
    };
    private static final String[] LAST_NAMES = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"
    };
    private static final String[] FIRST_NAMES = {
        "민준", "서연", "도현", "지훈", "수진", "유진", "현우", "하늘", "지아", "시윤"
    };
    private static final String[] COURSE_BASE_NAMES = {
        "자료구조", "운영체제", "알고리즘", "컴퓨터네트워크", "데이터베이스",
        "회계원리", "재무관리", "마케팅", "전자회로", "디지털논리",
        "선형대수", "확률과통계", "일반화학", "일반물리", "기계설계",
        "심리학개론", "사회학개론", "영문학개론", "경제학원론", "통계학개론"
    };
    private static final String[] COURSE_SUFFIXES = {"", "(기초)", "(응용)", "(심화)"};
    private static final String[] DAY_NAMES = {"월", "화", "수", "목", "금"};
    private static final String[] TIME_SLOTS = {
        "09:00-10:30", "10:30-12:00", "13:00-14:30", "14:30-16:00", "16:00-17:30"
    };
    private static final String[] NAME_POOL = buildNamePool();
    private static final String[] COURSE_NAME_POOL = buildCourseNamePool();
    private static final String[] SCHEDULE_POOL = buildSchedulePool();

    private final InMemoryStore store;

    public SeedDataGenerator(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        SeedData data = generate(start);
        store.setData(data);
    }

    private SeedData generate(long start) {
        Random random = new Random(SEED);

        long t0 = System.currentTimeMillis();
        List<SeedData.Department> departments = generateDepartments();
        long t1 = System.currentTimeMillis();
        log.info("seed departments ms={}", (t1 - t0));

        List<SeedData.Professor> professors = generateProfessors(random, departments);
        long t2 = System.currentTimeMillis();
        log.info("seed professors ms={}", (t2 - t1));

        List<SeedData.Course> courses = generateCourses(random, departments, professors);
        long t3 = System.currentTimeMillis();
        log.info("seed courses ms={}", (t3 - t2));

        List<SeedData.Student> students = generateStudents(random, departments);
        long t4 = System.currentTimeMillis();
        log.info("seed students ms={}", (t4 - t3));
        log.info("seed total ms={}", (t4 - start));

        return new SeedData(departments, professors, courses, students);
    }

    private List<SeedData.Department> generateDepartments() {
        List<SeedData.Department> departments = new ArrayList<>(Math.max(DEPARTMENT_MIN, DEPARTMENT_NAMES.length));
        int id = 10;
        for (String name : DEPARTMENT_NAMES) {
            departments.add(new SeedData.Department(id++, name));
        }
        while (departments.size() < DEPARTMENT_MIN) {
            departments.add(new SeedData.Department(id++, "학과" + id));
        }
        return departments;
    }

    private List<SeedData.Professor> generateProfessors(Random random, List<SeedData.Department> departments) {
        List<SeedData.Professor> professors = new ArrayList<>(PROFESSOR_MIN);

        int id = 500;
        for (int i = 0; i < PROFESSOR_MIN; i++) {
            String name = NAME_POOL[random.nextInt(NAME_POOL.length)];
            SeedData.Department department = departments.get(random.nextInt(departments.size()));
            professors.add(new SeedData.Professor(id++, name, department.id(), department.name()));
        }
        return professors;
    }

    private List<SeedData.Course> generateCourses(
        Random random,
        List<SeedData.Department> departments,
        List<SeedData.Professor> professors
    ) {
        List<SeedData.Course> courses = new ArrayList<>(COURSE_MIN);

        int id = 1;
        for (int i = 0; i < COURSE_MIN; i++) {
            SeedData.Department department = departments.get(random.nextInt(departments.size()));
            SeedData.Professor professor = professors.get(random.nextInt(professors.size()));
            String name = COURSE_NAME_POOL[random.nextInt(COURSE_NAME_POOL.length)];
            int credits = 3;
            int capacity = 20 + random.nextInt(41);
            int enrolled = random.nextInt(capacity + 1);
            String schedule = SCHEDULE_POOL[random.nextInt(SCHEDULE_POOL.length)];
            courses.add(new SeedData.Course(
                id++,
                name,
                credits,
                capacity,
                enrolled,
                schedule,
                department.id(),
                department.name(),
                professor.id(),
                professor.name()
            ));
        }
        return courses;
    }

    private List<SeedData.Student> generateStudents(Random random, List<SeedData.Department> departments) {
        List<SeedData.Student> students = new ArrayList<>(STUDENT_MIN);

        int id = 1000;
        for (int i = 0; i < STUDENT_MIN; i++) {
            String name = NAME_POOL[random.nextInt(NAME_POOL.length)];
            SeedData.Department department = departments.get(random.nextInt(departments.size()));
            int enrolledCredits = random.nextInt(7) * 3;
            students.add(new SeedData.Student(
                id++,
                name,
                department.id(),
                department.name(),
                MAX_CREDITS,
                enrolledCredits
            ));
        }
        return students;
    }

    private static String[] buildNamePool() {
        String[] pool = new String[LAST_NAMES.length * FIRST_NAMES.length];
        int idx = 0;
        for (String last : LAST_NAMES) {
            for (String first : FIRST_NAMES) {
                pool[idx++] = last + first;
            }
        }
        return pool;
    }

    private static String[] buildCourseNamePool() {
        String[] pool = new String[COURSE_BASE_NAMES.length * COURSE_SUFFIXES.length];
        int idx = 0;
        for (String base : COURSE_BASE_NAMES) {
            for (String suffix : COURSE_SUFFIXES) {
                pool[idx++] = base + suffix;
            }
        }
        return pool;
    }

    private static String[] buildSchedulePool() {
        String[] pool = new String[DAY_NAMES.length * TIME_SLOTS.length];
        int idx = 0;
        for (String day : DAY_NAMES) {
            for (String time : TIME_SLOTS) {
                pool[idx++] = day + " " + time;
            }
        }
        return pool;
    }
}
