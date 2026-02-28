package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DomainModel.Course;

@Service
public class CourseService {

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

        Course course = new Course(generateCuid(), cleanCode, cleanName, professorId);
        return courseRepo.save(course);
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }
        return courseRepo.findByProfessorIdOrderByCreatedAtDesc(professorId);
    }

    public Course getCourseById(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }
        return courseRepo.findById(courseId).orElse(null);
    }

    private String generateCuid() {
        long now = System.currentTimeMillis();
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "c" + Long.toString(now, 36) + Integer.toString(randomPart, 36);
    }
}
