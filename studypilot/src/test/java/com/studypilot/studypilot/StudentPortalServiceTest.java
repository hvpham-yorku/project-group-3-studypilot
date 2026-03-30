package com.studypilot.studypilot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupMemberRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyQuestionOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyQuestionRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyResponseRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;

/**
 * StudentPortalServiceTest component.
 */
public class StudentPortalServiceTest {

    private CourseRepo courseRepo;
    private CourseEnrollmentRepo enrollmentRepo;
    private GroupFormationActivityRepo activityRepo;
    private SurveyQuestionRepo surveyQuestionRepo;
    private SurveyQuestionOptionRepo surveyQuestionOptionRepo;
    private SurveyResponseRepo surveyResponseRepo;
    private FormedGroupRepo formedGroupRepo;
    private FormedGroupMemberRepo formedGroupMemberRepo;
    private UserRepo userRepo;

    private StudentPortalService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        courseRepo = mock(CourseRepo.class);
        enrollmentRepo = mock(CourseEnrollmentRepo.class);
        activityRepo = mock(GroupFormationActivityRepo.class);
        surveyQuestionRepo = mock(SurveyQuestionRepo.class);
        surveyQuestionOptionRepo = mock(SurveyQuestionOptionRepo.class);
        surveyResponseRepo = mock(SurveyResponseRepo.class);
        formedGroupRepo = mock(FormedGroupRepo.class);
        formedGroupMemberRepo = mock(FormedGroupMemberRepo.class);
        userRepo = mock(UserRepo.class);

        service = new StudentPortalService(
                courseRepo,
                enrollmentRepo,
                activityRepo,
                surveyQuestionRepo,
                surveyQuestionOptionRepo,
                surveyResponseRepo,
                null,
                formedGroupRepo,
                formedGroupMemberRepo,
                userRepo
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.getStudentCourses(null);
        });
        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.enrollStudentInCourseByJoinCode(1L, "SHORT");
        });
        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.enrollStudentInCourseByJoinCode(1L, "ABCDEFGH");
        });

        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
        verify(enrollmentRepo, never()).save(any());
    }

    //  TEST: course not found
    @Test
    void testEnrollCourseNotFound() {
        when(courseRepo.findByJoinCode("ABCDEFGH"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.enrollStudentInCourseByJoinCode(1L, "ABCDEFGH");
        });
        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
    }

    //  TEST: getLatestGroupActivity
    @Test
    void testGetLatestActivity() {
        GroupFormationActivity activity = mock(GroupFormationActivity.class);
        when(activity.getId()).thenReturn(10L);

        when(activityRepo.findByCourseIdOrderByCreatedAtDesc("C1"))
                .thenReturn(List.of(activity));

        Optional<GroupFormationActivity> result
                = service.getLatestGroupActivityForCourse("C1");

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    //  TEST: no activity
    @Test
    void testGetLatestActivityEmpty() {
        when(activityRepo.findByCourseIdOrderByCreatedAtDesc("C1"))
                .thenReturn(new ArrayList<>());

        Optional<GroupFormationActivity> result
                = service.getLatestGroupActivityForCourse("C1");

        assertTrue(result.isEmpty());
    }
}
