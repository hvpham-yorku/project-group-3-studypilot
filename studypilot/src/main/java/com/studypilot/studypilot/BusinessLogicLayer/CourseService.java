package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DomainModel.Course;

@Service
/**
 * CourseService component.
 */
public class CourseService {

    private static final String JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int JOIN_CODE_LENGTH = 8;

    private final CourseRepo courseRepo;

    public CourseService(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    public Course createCourse(Long professorId, String courseCode, String courseName) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        String cleanCode = courseCode == null ? "" : courseCode.trim();
        String cleanName = courseName == null ? "" : courseName.trim();

        if (cleanCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }
        if (cleanName.isBlank()) {
            throw new IllegalArgumentException("Course name is required.");
        }

        Course course = new Course(generateCuid(), cleanCode, cleanName, professorId, generateUniqueJoinCode());
        return courseRepo.save(course);
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }
        return courseRepo.findByProfessorIdOrderByCreatedAtDesc(professorId)
                .stream()
                .map(this::ensureJoinCode)
                .collect(Collectors.toList());
    }

    public Course getCourseById(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }
        return courseRepo.findById(courseId)
                .map(this::ensureJoinCode)
                .orElse(null);
    }

    public Course getCourseByJoinCode(String joinCode) {
        String normalizedJoinCode = normalizeJoinCode(joinCode);
        if (normalizedJoinCode.isBlank()) {
            return null;
        }

        return courseRepo.findByJoinCode(normalizedJoinCode)
                .map(this::ensureJoinCode)
                .orElse(null);
    }

    private String generateCuid() {
        long now = System.currentTimeMillis();
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "c" + Long.toString(now, 36) + Integer.toString(randomPart, 36);
    }

    private String generateUniqueJoinCode() {
        String joinCode;
        do {
            joinCode = generateJoinCode();
        } while (courseRepo.existsByJoinCode(joinCode));
        return joinCode;
    }

    private String generateJoinCode() {
        StringBuilder builder = new StringBuilder(JOIN_CODE_LENGTH);
        for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
            int index = ThreadLocalRandom.current().nextInt(JOIN_CODE_ALPHABET.length());
            builder.append(JOIN_CODE_ALPHABET.charAt(index));
        }
        return builder.toString();
    }

    private Course ensureJoinCode(Course course) {
        if (course == null) {
            return null;
        }

        String normalizedJoinCode = normalizeJoinCode(course.getJoinCode());
        if (normalizedJoinCode.length() == JOIN_CODE_LENGTH) {
            if (!normalizedJoinCode.equals(course.getJoinCode())) {
                course.setJoinCode(normalizedJoinCode);
                return courseRepo.save(course);
            }
            return course;
        }

        course.setJoinCode(generateUniqueJoinCode());
        return courseRepo.save(course);
    }

    private String normalizeJoinCode(String joinCode) {
        return joinCode == null ? "" : joinCode.trim().toUpperCase();
    }
}
