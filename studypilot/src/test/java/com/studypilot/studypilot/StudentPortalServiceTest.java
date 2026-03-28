package com.studypilot.studypilot;

import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.DataAccessLayer.*;
import com.studypilot.studypilot.DomainModel.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudentPortalServiceTest {

    private CourseRepo courseRepo;
    private CourseEnrollmentRepo enrollmentRepo;
    private GroupFormationActivityRepo activityRepo;
    private GroupFormationTopicOptionRepo topicRepo;
    private GroupFormationSkillOptionRepo skillRepo;
    private StudentGroupPreferenceRepo preferenceRepo;

    private StudentPortalService service;

    @BeforeEach
    void setup() {
        courseRepo = mock(CourseRepo.class);
        enrollmentRepo = mock(CourseEnrollmentRepo.class);
        activityRepo = mock(GroupFormationActivityRepo.class);
        topicRepo = mock(GroupFormationTopicOptionRepo.class);
        skillRepo = mock(GroupFormationSkillOptionRepo.class);
        preferenceRepo = mock(StudentGroupPreferenceRepo.class);

        service = new StudentPortalService(
                courseRepo,
                enrollmentRepo,
                activityRepo,
                topicRepo,
                skillRepo,
                preferenceRepo
        );
    }

    //  TEST: getStudentCourses
    @Test
    void testGetStudentCourses() {
        CourseEnrollment enrollment = new CourseEnrollment("C1", 1L);

        Course course = mock(Course.class);
        when(course.getId()).thenReturn("C1");

        when(enrollmentRepo.findByStudentIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(enrollment));

        when(courseRepo.findById("C1"))
                .thenReturn(Optional.of(course));

        List<Course> result = service.getStudentCourses(1L);

        assertEquals(1, result.size());
        assertEquals("C1", result.get(0).getId());
    }

    //  TEST: null student
    @Test
    void testGetStudentCoursesNullStudent() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getStudentCourses(null);
        });
    }

    //  TEST: enroll by join code success
    @Test
    void testEnrollByJoinCodeSuccess() {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn("C1");
        when(course.getJoinCode()).thenReturn("ABCDEFGH");

        when(courseRepo.findByJoinCode("ABCDEFGH"))
                .thenReturn(Optional.of(course));

        when(enrollmentRepo.existsByCourseIdAndStudentId("C1", 1L))
                .thenReturn(false);

        service.enrollStudentInCourseByJoinCode(1L, "ABCDEFGH");

        verify(enrollmentRepo, times(1)).save(any());
    }

    //  TEST: invalid join code length
    @Test
    void testEnrollByJoinCodeInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.enrollStudentInCourseByJoinCode(1L, "SHORT");
        });
    }

    //  TEST: already enrolled (should NOT save)
    @Test
    void testEnrollAlreadyExists() {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn("C1");

        when(courseRepo.findByJoinCode("ABCDEFGH"))
                .thenReturn(Optional.of(course));

        when(enrollmentRepo.existsByCourseIdAndStudentId("C1", 1L))
                .thenReturn(true);

        service.enrollStudentInCourseByJoinCode(1L, "ABCDEFGH");

        verify(enrollmentRepo, never()).save(any());
    }

    //  TEST: course not found
    @Test
    void testEnrollCourseNotFound() {
        when(courseRepo.findByJoinCode("ABCDEFGH"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            service.enrollStudentInCourseByJoinCode(1L, "ABCDEFGH");
        });
    }

    //  TEST: getLatestGroupActivity
    @Test
    void testGetLatestActivity() {
        GroupFormationActivity activity = mock(GroupFormationActivity.class);
        when(activity.getId()).thenReturn(10L);

        when(activityRepo.findByCourseIdOrderByCreatedAtDesc("C1"))
                .thenReturn(List.of(activity));

        Optional<GroupFormationActivity> result =
                service.getLatestGroupActivityForCourse("C1");

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    //  TEST: no activity
    @Test
    void testGetLatestActivityEmpty() {
        when(activityRepo.findByCourseIdOrderByCreatedAtDesc("C1"))
                .thenReturn(new ArrayList<>());

        Optional<GroupFormationActivity> result =
                service.getLatestGroupActivityForCourse("C1");

        assertTrue(result.isEmpty());
    }
}