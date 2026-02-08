package com.musinsa.course.data;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataGenerator implements ApplicationRunner {
    private static final int DEPARTMENT_MIN = 10;
    private static final int PROFESSOR_MIN = 100;
    private static final int COURSE_MIN = 500;
    private static final int STUDENT_MIN = 10_000;
    private static final long SEED = 42L;

    private final InMemoryStore store;

    public SeedDataGenerator(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void run(ApplicationArguments args) {
        SeedData data = generate();
        store.setData(data);
    }

    private SeedData generate() {
        Random random = new Random(SEED);

        List<SeedData.Department> departments = generateDepartments();
        List<SeedData.Professor> professors = generateProfessors(random, departments);
        List<SeedData.Course> courses = generateCourses(random, departments, professors);
        List<SeedData.Student> students = generateStudents(random, departments);

        return new SeedData(departments, professors, courses, students);
    }

    private List<SeedData.Department> generateDepartments() {
        List<SeedData.Department> departments = new ArrayList<>();
        String[] names = {
            "컴퓨터공학과", "경영학과", "전자공학과", "수학과", "영문학과",
            "경제학과", "물리학과", "화학과", "기계공학과", "심리학과",
            "사회학과", "통계학과"
        };
        int id = 10;
        for (String name : names) {
            departments.add(new SeedData.Department(id++, name));
        }
        while (departments.size() < DEPARTMENT_MIN) {
            departments.add(new SeedData.Department(id++, "학과" + id));
        }
        return departments;
    }

    private List<SeedData.Professor> generateProfessors(Random random, List<SeedData.Department> departments) {
        List<SeedData.Professor> professors = new ArrayList<>(PROFESSOR_MIN);
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
        String[] firstNames = {"민준", "서연", "도현", "지훈", "수진", "유진", "현우", "하늘", "지아", "시윤"};

        int id = 500;
        for (int i = 0; i < PROFESSOR_MIN; i++) {
            String name = lastNames[random.nextInt(lastNames.length)]
                + firstNames[random.nextInt(firstNames.length)];
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
        String[] baseNames = {
            "자료구조", "운영체제", "알고리즘", "컴퓨터네트워크", "데이터베이스",
            "회계원리", "재무관리", "마케팅", "전자회로", "디지털논리",
            "선형대수", "확률과통계", "일반화학", "일반물리", "기계설계",
            "심리학개론", "사회학개론", "영문학개론", "경제학원론", "통계학개론"
        };
        String[] suffixes = {"", "(기초)", "(응용)", "(심화)"};
        DayOfWeek[] days = {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        };
        String[] timeSlots = {
            "09:00-10:30", "10:30-12:00", "13:00-14:30", "14:30-16:00", "16:00-17:30"
        };

        int id = 1;
        for (int i = 0; i < COURSE_MIN; i++) {
            SeedData.Department department = departments.get(random.nextInt(departments.size()));
            SeedData.Professor professor = professors.get(random.nextInt(professors.size()));
            String baseName = baseNames[random.nextInt(baseNames.length)];
            String name = baseName + suffixes[random.nextInt(suffixes.length)];
            int credits = 3;
            int capacity = 20 + random.nextInt(41);
            int enrolled = random.nextInt(capacity + 1);
            DayOfWeek day = days[random.nextInt(days.length)];
            String schedule = dayToKorean(day) + " " + timeSlots[random.nextInt(timeSlots.length)];
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
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
        String[] firstNames = {"민준", "서연", "도현", "지훈", "수진", "유진", "현우", "하늘", "지아", "시윤"};

        int id = 1000;
        for (int i = 0; i < STUDENT_MIN; i++) {
            String name = lastNames[random.nextInt(lastNames.length)]
                + firstNames[random.nextInt(firstNames.length)];
            SeedData.Department department = departments.get(random.nextInt(departments.size()));
            int enrolledCredits = random.nextInt(7) * 3;
            students.add(new SeedData.Student(
                id++,
                name,
                department.id(),
                department.name(),
                18,
                enrolledCredits
            ));
        }
        return students;
    }

    private String dayToKorean(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            default -> "월";
        };
    }
}
