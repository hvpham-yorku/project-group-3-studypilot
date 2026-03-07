package com.studypilot.studypilot.DataAccessLayer;

import com.studypilot.studypilot.DomainModel.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest  // Spins up an in-memory database for testing JPA repositories
class CourseRepoTest {

    @Autowired
    private CourseRepo courseRepo;

    @Test
    void saveAndFindById() {
        // Arrange
        Course course = new Course("c123", "CS101", "Intro to CS", 1L);
        courseRepo.save(course);

        // Act
        Course found = courseRepo.findById("c123").orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals("CS101", found.getCourseCode());
        assertEquals("Intro to CS", found.getCourseName());
    }

    @Test
    void findByProfessorIdOrderByCreatedAtDesc_returnsCourses() {
        // Arrange
        Course course1 = new Course("c1", "CS101", "Intro to CS", 1L);
        Course course2 = new Course("c2", "CS102", "Data Structures", 1L);
        Course course3 = new Course("c3", "CS103", "Algorithms", 2L);
        courseRepo.save(course1);
        courseRepo.save(course2);
        courseRepo.save(course3);

        // Act
        List<Course> courses = courseRepo.findByProfessorIdOrderByCreatedAtDesc(1L);

        // Assert
        assertEquals(2, courses.size());
        assertTrue(courses.stream().allMatch(c -> c.getProfessorId().equals(1L)));
    }

    @Test
    void findByProfessorIdOrderByCreatedAtDesc_noCourses_returnsEmpty() {
        List<Course> courses = courseRepo.findByProfessorIdOrderByCreatedAtDesc(99L);
        assertTrue(courses.isEmpty());
    }
}