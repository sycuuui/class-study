package com.musinsa.course.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
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
    private final Map<Integer, ReentrantLock> courseLocks = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> studentLocks = new ConcurrentHashMap<>();
    private final Set<EnrollmentKey> enrollments = new HashSet<>();
    private final Map<EnrollmentKey, Long> enrollmentIds = new HashMap<>();
    private final AtomicLong enrollmentSeq = new AtomicLong(900_000);

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

    public EnrollmentResult enroll(int studentId, int courseId) {
        if (studentId <= 0 || courseId <= 0) {
            return EnrollmentResult.invalidRequest();
        }
        SeedData.Student student = studentsById.get(studentId);
        if (student == null) {
            return EnrollmentResult.studentNotFound(studentId);
        }
        CourseState course = coursesById.get(courseId);
        if (course == null) {
            return EnrollmentResult.courseNotFound(courseId);
        }
        ReentrantLock courseLock = courseLocks.computeIfAbsent(courseId, ignored -> new ReentrantLock());
        ReentrantLock studentLock = studentLocks.computeIfAbsent(studentId, ignored -> new ReentrantLock());
        courseLock.lock();
        try {
            studentLock.lock();
            try {
                EnrollmentKey key = new EnrollmentKey(studentId, courseId);
                if (enrollments.contains(key)) {
                    return EnrollmentResult.duplicateEnrollment(studentId, courseId);
                }
                if (course.enrolled >= course.capacity) {
                    return EnrollmentResult.capacityExceeded(courseId);
                }
                int currentCredits = 0;
                Schedule newSchedule = Schedule.parse(course.schedule);
                for (EnrollmentKey existing : enrollments) {
                    if (existing.studentId != studentId) {
                        continue;
                    }
                    CourseState enrolledCourse = coursesById.get(existing.courseId);
                    if (enrolledCourse == null) {
                        continue;
                    }
                    currentCredits += enrolledCourse.credits;
                    Schedule existingSchedule = Schedule.parse(enrolledCourse.schedule);
                    if (newSchedule.overlaps(existingSchedule)) {
                        return EnrollmentResult.timeConflict(studentId, courseId);
                    }
                }
                if (currentCredits + course.credits > 18) {
                    return EnrollmentResult.creditLimitExceeded(studentId, courseId);
                }
                course.enrolled++;
                long enrollmentId = enrollmentSeq.incrementAndGet();
                enrollments.add(key);
                enrollmentIds.put(key, enrollmentId);
                return EnrollmentResult.success(enrollmentId, studentId, courseId);
            } finally {
                studentLock.unlock();
            }
        } finally {
            courseLock.unlock();
        }
    }

    public EnrollmentResult cancel(int studentId, int courseId) {
        if (studentId <= 0 || courseId <= 0) {
            return EnrollmentResult.invalidRequest();
        }
        SeedData.Student student = studentsById.get(studentId);
        if (student == null) {
            return EnrollmentResult.studentNotFound(studentId);
        }
        CourseState course = coursesById.get(courseId);
        if (course == null) {
            return EnrollmentResult.courseNotFound(courseId);
        }
        ReentrantLock courseLock = courseLocks.computeIfAbsent(courseId, ignored -> new ReentrantLock());
        ReentrantLock studentLock = studentLocks.computeIfAbsent(studentId, ignored -> new ReentrantLock());
        courseLock.lock();
        try {
            studentLock.lock();
            try {
                EnrollmentKey key = new EnrollmentKey(studentId, courseId);
                if (!enrollments.contains(key)) {
                    return EnrollmentResult.enrollmentNotFound(studentId, courseId);
                }
                enrollments.remove(key);
                enrollmentIds.remove(key);
                course.enrolled = Math.max(0, course.enrolled - 1);
                return EnrollmentResult.canceled();
            } finally {
                studentLock.unlock();
            }
        } finally {
            courseLock.unlock();
        }
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

    private record EnrollmentKey(int studentId, int courseId) {
    }

    public static final class EnrollmentResult {
        public final boolean success;
        public final boolean canceled;
        public final long enrollmentId;
        public final int studentId;
        public final int courseId;
        public final int errorCode;
        public final int httpStatus;
        public final String message;

        private EnrollmentResult(
            boolean success,
            boolean canceled,
            long enrollmentId,
            int studentId,
            int courseId,
            int errorCode,
            int httpStatus,
            String message
        ) {
            this.success = success;
            this.canceled = canceled;
            this.enrollmentId = enrollmentId;
            this.studentId = studentId;
            this.courseId = courseId;
            this.errorCode = errorCode;
            this.httpStatus = httpStatus;
            this.message = message;
        }

        public static EnrollmentResult success(long enrollmentId, int studentId, int courseId) {
            return new EnrollmentResult(true, false, enrollmentId, studentId, courseId, 0, 201, "enrolled");
        }

        public static EnrollmentResult canceled() {
            return new EnrollmentResult(false, true, 0, 0, 0, 0, 204, "canceled");
        }

        public static EnrollmentResult invalidRequest() {
            return new EnrollmentResult(false, false, 0, 0, 0, 500, 400, "잘못된 요청 파라미터");
        }

        public static EnrollmentResult studentNotFound(int studentId) {
            return new EnrollmentResult(false, false, 0, studentId, 0, 501, 404, "학생을 찾을 수 없습니다");
        }

        public static EnrollmentResult courseNotFound(int courseId) {
            return new EnrollmentResult(false, false, 0, 0, courseId, 502, 404, "강좌를 찾을 수 없습니다");
        }

        public static EnrollmentResult enrollmentNotFound(int studentId, int courseId) {
            return new EnrollmentResult(false, false, 0, studentId, courseId, 504, 404, "수강신청 내역이 없습니다");
        }

        public static EnrollmentResult capacityExceeded(int courseId) {
            return new EnrollmentResult(false, false, 0, 0, courseId, 600, 409, "정원이 초과되어 신청할 수 없습니다");
        }

        public static EnrollmentResult duplicateEnrollment(int studentId, int courseId) {
            return new EnrollmentResult(false, false, 0, studentId, courseId, 603, 409, "이미 신청한 강좌입니다");
        }

        public static EnrollmentResult creditLimitExceeded(int studentId, int courseId) {
            return new EnrollmentResult(false, false, 0, studentId, courseId, 601, 409, "최대 학점을 초과합니다");
        }

        public static EnrollmentResult timeConflict(int studentId, int courseId) {
            return new EnrollmentResult(false, false, 0, studentId, courseId, 602, 409, "시간표가 충돌합니다");
        }
    }

    private record Schedule(String day, int start, int end) {

        private static Schedule parse(String schedule) {
                String[] parts = schedule.split(" ");
                String day = parts[0];
                String[] times = parts[1].split("-");
                int start = toMinutes(times[0]);
                int end = toMinutes(times[1]);
                return new Schedule(day, start, end);
            }

            private static int toMinutes(String hhmm) {
                int colon = hhmm.indexOf(':');
                int hour = Integer.parseInt(hhmm.substring(0, colon));
                int minute = Integer.parseInt(hhmm.substring(colon + 1));
                return hour * 60 + minute;
            }

            private boolean overlaps(Schedule other) {
                if (!this.day.equals(other.day)) {
                    return false;
                }
                return this.start < other.end && other.start < this.end;
            }
        }
}
