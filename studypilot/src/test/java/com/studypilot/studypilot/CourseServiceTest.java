package com.studypilot.studypilot;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DomainModel.Course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
/**
 * CourseServiceTest component.
 */
class CourseServiceTest {

    @Mock
    private CourseRepo courseRepo;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepo);
    }

    // --- createCourse ---

    @Test
    void createCourse_validInputs_savesCourse() {
        when(courseRepo.existsByJoinCode(any())).thenReturn(false);
        when(courseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.createCourse(1L, "CS101", "Intro to CS");

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        assertEquals("Intro to CS", result.getCourseName());
        assertEquals(1L, result.getProfessorId());
        verify(courseRepo, times(1)).save(any());
    }

    @Test
    void createCourse_nullProfessorId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(null, "CS101", "Intro to CS"));
        assertEquals("Professor must be logged in.", ex.getMessage());
    }

    @Test
    void createCourse_blankCourseCode_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(1L, "  ", "Intro to CS"));
        assertEquals("Course code is required.", ex.getMessage());
    }

    @Test
    void createCourse_nullCourseCode_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(1L, null, "Intro to CS"));
        assertEquals("Course code is required.", ex.getMessage());
    }

    @Test
    void createCourse_blankCourseName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(1L, "CS101", "  "));
        assertEquals("Course name is required.", ex.getMessage());
    }

    @Test
    void createCourse_nullCourseName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(1L, "CS101", null));
        assertEquals("Course name is required.", ex.getMessage());
    }

    @Test
    void createCourse_trimsWhitespace() {
        when(courseRepo.existsByJoinCode(any())).thenReturn(false);
        when(courseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.createCourse(1L, "  CS101  ", "  Intro to CS  ");

        assertEquals("CS101", result.getCourseCode());
        assertEquals("Intro to CS", result.getCourseName());
    }

    // --- getCoursesForProfessor ---

    @Test
    void getCoursesForProfessor_returnsCourses() {
        Course c1 = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        Course c2 = new Course("c2", "CS102", "Data Structures", 1L, "EFGH5678");
        when(courseRepo.findByProfessorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(c1, c2));
        lenient().when(courseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        List<Course> result = courseService.getCoursesForProfessor(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getCoursesForProfessor_nullProfessorId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.getCoursesForProfessor(null));
        assertEquals("Professor must be logged in.", ex.getMessage());
    }

    @Test
    void getCoursesForProfessor_noCourses_returnsEmpty() {
        when(courseRepo.findByProfessorIdOrderByCreatedAtDesc(99L)).thenReturn(List.of());

        List<Course> result = courseService.getCoursesForProfessor(99L);

        assertTrue(result.isEmpty());
    }

    // --- getCourseById ---

    @Test
    void getCourseById_validId_returnsCourse() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        lenient().when(courseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.getCourseById("c1");

        assertNotNull(result);
        assertEquals("c1", result.getId());
    }

    @Test
    void getCourseById_nullId_returnsNull() {
        Course result = courseService.getCourseById(null);
        assertNull(result);
    }

    @Test
    void getCourseById_blankId_returnsNull() {
        Course result = courseService.getCourseById("  ");
        assertNull(result);
    }

    @Test
    void getCourseById_notFound_returnsNull() {
        when(courseRepo.findById("missing")).thenReturn(Optional.empty());

        Course result = courseService.getCourseById("missing");

        assertNull(result);
    }

    // --- getCourseByJoinCode ---

    @Test
    void getCourseByJoinCode_validCode_returnsCourse() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        when(courseRepo.findByJoinCode("ABCD1234")).thenReturn(Optional.of(course));
        lenient().when(courseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.getCourseByJoinCode("ABCD1234");

        assertNotNull(result);
        assertEquals("ABCD1234", result.getJoinCode());
    }

    @Test
    void getCourseByJoinCode_nullCode_returnsNull() {
        Course result = courseService.getCourseByJoinCode(null);
        assertNull(result);
    }

    @Test
    void getCourseByJoinCode_blankCode_returnsNull() {
        Course result = courseService.getCourseByJoinCode("  ");
        assertNull(result);
    }

    @Test
    void getCourseByJoinCode_notFound_returnsNull() {
        when(courseRepo.findByJoinCode("ZZZZZZZZ")).thenReturn(Optional.empty());

        Course result = courseService.getCourseByJoinCode("ZZZZZZZZ");

        assertNull(result);
    }
}
