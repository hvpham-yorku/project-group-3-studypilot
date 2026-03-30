package com.studypilot.studypilot;

import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamHealthCheckinRepo;
import com.studypilot.studypilot.DataAccessLayer.WeeklySurveyRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;
import com.studypilot.studypilot.DomainModel.WeeklySurvey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
/**
 * TeamHealthServiceTest component.
 */
class TeamHealthServiceTest {

    @Mock
    private CourseRepo courseRepo;
    @Mock
    private CourseEnrollmentRepo courseEnrollmentRepo;
    @Mock
    private TeamHealthCheckinRepo teamHealthCheckinRepo;
    @Mock
    private WeeklySurveyRepo weeklySurveyRepo;

    private TeamHealthService teamHealthService;

    private static final LocalDate WEEK = LocalDate.of(2026, 3, 23);

    @BeforeEach
    void setUp() {
        teamHealthService = new TeamHealthService(
                courseRepo,
                courseEnrollmentRepo,
                teamHealthCheckinRepo,
                weeklySurveyRepo);
    }

    // --- getStudentCheckinHistory ---
    @Test
    void getStudentCheckinHistory_returnsHistoryOrderedByWeek() {
        LocalDate week1 = LocalDate.of(2026, 3, 16);
        TeamHealthCheckin c1 = new TeamHealthCheckin("course1", 1L, WEEK, 4, 4, 4, "Good week");
        TeamHealthCheckin c2 = new TeamHealthCheckin("course1", 1L, week1, 3, 3, 3, "Okay week");
        when(teamHealthCheckinRepo.findByStudentIdOrderByWeekStartDesc(1L))
                .thenReturn(List.of(c1, c2));

        List<TeamHealthCheckin> result = teamHealthService.getStudentCheckinHistory(1L);

        assertEquals(2, result.size());
        assertEquals(WEEK, result.get(0).getWeekStart());
        assertEquals(week1, result.get(1).getWeekStart());
    }

    @Test
    void getStudentCheckinHistory_noHistory_returnsEmpty() {
        when(teamHealthCheckinRepo.findByStudentIdOrderByWeekStartDesc(1L))
                .thenReturn(List.of());

        List<TeamHealthCheckin> result = teamHealthService.getStudentCheckinHistory(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getStudentCheckinHistory_nullStudentId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.getStudentCheckinHistory(null));
        assertEquals("Student must be logged in.", ex.getMessage());
    }

    @Test
    void getStudentCheckinHistory_multipleCoursesReturnsAll() {
        TeamHealthCheckin c1 = new TeamHealthCheckin("course1", 1L, WEEK, 5, 5, 5, "");
        TeamHealthCheckin c2 = new TeamHealthCheckin("course2", 1L, WEEK, 2, 3, 4, "");
        when(teamHealthCheckinRepo.findByStudentIdOrderByWeekStartDesc(1L))
                .thenReturn(List.of(c1, c2));

        List<TeamHealthCheckin> result = teamHealthService.getStudentCheckinHistory(1L);

        assertEquals(2, result.size());
        assertEquals("course1", result.get(0).getCourseId());
        assertEquals("course2", result.get(1).getCourseId());
    }

    @Test
    void getStudentCheckinHistory_scoresAreCorrect() {
        TeamHealthCheckin checkin = new TeamHealthCheckin("course1", 1L, WEEK, 3, 4, 5, "notes");
        when(teamHealthCheckinRepo.findByStudentIdOrderByWeekStartDesc(1L))
                .thenReturn(List.of(checkin));

        List<TeamHealthCheckin> result = teamHealthService.getStudentCheckinHistory(1L);

        assertEquals(3, result.get(0).getHealthScore());
        assertEquals(4, result.get(0).getWorkloadScore());
        assertEquals(5, result.get(0).getCollaborationScore());
        assertEquals("notes", result.get(0).getStatusText());
    }

    // --- getWeekStart ---
    @Test
    void getWeekStart_returnsMonday() {
        LocalDate wednesday = LocalDate.of(2026, 3, 25);
        LocalDate result = teamHealthService.getWeekStart(wednesday);
        assertEquals(LocalDate.of(2026, 3, 23), result);
    }

    @Test
    void getWeekStart_alreadyMonday_returnsSameDay() {
        LocalDate result = teamHealthService.getWeekStart(WEEK);
        assertEquals(WEEK, result);
    }

    @Test
    void getWeekStart_nullDate_returnsCurrentWeekMonday() {
        LocalDate result = teamHealthService.getWeekStart(null);
        assertNotNull(result);
        assertEquals(java.time.DayOfWeek.MONDAY, result.getDayOfWeek());
    }

    // --- saveWeeklyCheckin ---
    @Test
    void saveWeeklyCheckin_nullStudentId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.saveWeeklyCheckin(null, "course1", 3, 3, 3, "", WEEK));
        assertEquals("Student must be logged in.", ex.getMessage());
    }

    @Test
    void saveWeeklyCheckin_courseNotFound_throwsException() {
        when(courseRepo.findById("bad")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.saveWeeklyCheckin(1L, "bad", 3, 3, 3, "", WEEK));
        assertEquals("Course not found.", ex.getMessage());
    }

    @Test
    void saveWeeklyCheckin_notEnrolled_throwsException() {
        Course course = new Course("c1", "CS101", "Intro to CS", 10L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(courseEnrollmentRepo.existsByCourseIdAndStudentId("c1", 1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.saveWeeklyCheckin(1L, "c1", 3, 3, 3, "", WEEK));
        assertEquals("You are not enrolled in this course.", ex.getMessage());
    }

    @Test
    void saveWeeklyCheckin_invalidHealthScore_throwsException() {
        Course course = new Course("c1", "CS101", "Intro to CS", 10L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(courseEnrollmentRepo.existsByCourseIdAndStudentId("c1", 1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.saveWeeklyCheckin(1L, "c1", 6, 3, 3, "", WEEK));
        assertEquals("Health score must be between 1 and 5.", ex.getMessage());
    }

    @Test
    void saveWeeklyCheckin_noSurveyPublished_throwsException() {
        Course course = new Course("c1", "CS101", "Intro to CS", 10L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(courseEnrollmentRepo.existsByCourseIdAndStudentId("c1", 1L)).thenReturn(true);
        when(weeklySurveyRepo.existsByCourseIdAndWeekStart("c1", WEEK)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.saveWeeklyCheckin(1L, "c1", 3, 3, 3, "", WEEK));
        assertEquals("No weekly survey has been published for this course yet.", ex.getMessage());
    }

    @Test
    void saveWeeklyCheckin_newCheckin_savesSuccessfully() {
        Course course = new Course("c1", "CS101", "Intro to CS", 10L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(courseEnrollmentRepo.existsByCourseIdAndStudentId("c1", 1L)).thenReturn(true);
        when(weeklySurveyRepo.existsByCourseIdAndWeekStart("c1", WEEK)).thenReturn(true);
        when(teamHealthCheckinRepo.findByCourseIdAndStudentIdAndWeekStart("c1", 1L, WEEK))
                .thenReturn(Optional.empty());
        when(teamHealthCheckinRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        TeamHealthCheckin result = teamHealthService.saveWeeklyCheckin(1L, "c1", 3, 4, 5, "notes", WEEK);

        assertNotNull(result);
        assertEquals(3, result.getHealthScore());
        assertEquals(4, result.getWorkloadScore());
        assertEquals(5, result.getCollaborationScore());
        assertEquals("notes", result.getStatusText());
    }

    @Test
    void saveWeeklyCheckin_existingCheckin_updatesSuccessfully() {
        Course course = new Course("c1", "CS101", "Intro to CS", 10L, "ABCD1234");
        TeamHealthCheckin existing = new TeamHealthCheckin("c1", 1L, WEEK, 2, 2, 2, "old");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(courseEnrollmentRepo.existsByCourseIdAndStudentId("c1", 1L)).thenReturn(true);
        when(weeklySurveyRepo.existsByCourseIdAndWeekStart("c1", WEEK)).thenReturn(true);
        when(teamHealthCheckinRepo.findByCourseIdAndStudentIdAndWeekStart("c1", 1L, WEEK))
                .thenReturn(Optional.of(existing));
        when(teamHealthCheckinRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        TeamHealthCheckin result = teamHealthService.saveWeeklyCheckin(1L, "c1", 5, 5, 5, "updated", WEEK);

        assertEquals(5, result.getHealthScore());
        assertEquals(5, result.getWorkloadScore());
        assertEquals(5, result.getCollaborationScore());
        assertEquals("updated", result.getStatusText());
    }

    // --- publishWeeklySurvey ---
    @Test
    void publishWeeklySurvey_nullProfessorId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.publishWeeklySurvey(null, "c1", "Title", "Desc", WEEK));
        assertEquals("Professor must be logged in.", ex.getMessage());
    }

    @Test
    void publishWeeklySurvey_courseNotFound_throwsException() {
        when(courseRepo.findById("bad")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.publishWeeklySurvey(1L, "bad", "Title", "Desc", WEEK));
        assertEquals("Course not found.", ex.getMessage());
    }

    @Test
    void publishWeeklySurvey_wrongProfessor_throwsException() {
        Course course = new Course("c1", "CS101", "Intro to CS", 99L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.publishWeeklySurvey(1L, "c1", "Title", "Desc", WEEK));
        assertEquals("You can only publish surveys for your own course.", ex.getMessage());
    }

    @Test
    void publishWeeklySurvey_blankTitle_throwsException() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.publishWeeklySurvey(1L, "c1", "  ", "Desc", WEEK));
        assertEquals("Survey title is required.", ex.getMessage());
    }

    @Test
    void publishWeeklySurvey_newSurvey_savesSuccessfully() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(weeklySurveyRepo.findByCourseIdAndWeekStart("c1", WEEK)).thenReturn(Optional.empty());
        when(weeklySurveyRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklySurvey result = teamHealthService.publishWeeklySurvey(1L, "c1", "Title", "Desc", WEEK);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("Desc", result.getDescription());
    }

    @Test
    void publishWeeklySurvey_existingSurvey_updatesSuccessfully() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        WeeklySurvey existing = new WeeklySurvey("c1", 1L, WEEK, "Old Title", "Old Desc");
        when(courseRepo.findById("c1")).thenReturn(Optional.of(course));
        when(weeklySurveyRepo.findByCourseIdAndWeekStart("c1", WEEK)).thenReturn(Optional.of(existing));
        when(weeklySurveyRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklySurvey result = teamHealthService.publishWeeklySurvey(1L, "c1", "New Title", "New Desc", WEEK);

        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
    }

    // --- getProfessorWeeklySummary ---
    @Test
    void getProfessorWeeklySummary_nullProfessorId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.getProfessorWeeklySummary(null, WEEK));
        assertEquals("Professor must be logged in.", ex.getMessage());
    }

    @Test
    void getProfessorWeeklySummary_noCourses_returnsZeroSummary() {
        when(courseRepo.findByProfessorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        TeamHealthService.ProfessorHealthSummary result
                = teamHealthService.getProfessorWeeklySummary(1L, WEEK);

        assertEquals(0, result.totalSubmissions());
        assertEquals(0, result.avgHealthPercent());
        assertEquals(0, result.atRiskResponses());
        assertEquals(0, result.missingSurveys());
    }

    @Test
    void getProfessorWeeklySummary_withSubmissions_returnsCorrectCounts() {
        Course course = new Course("c1", "CS101", "Intro to CS", 1L, "ABCD1234");
        CourseEnrollment e1 = new CourseEnrollment("c1", 2L);
        CourseEnrollment e2 = new CourseEnrollment("c1", 3L);
        TeamHealthCheckin checkin1 = new TeamHealthCheckin("c1", 2L, WEEK, 4, 4, 4, "");
        TeamHealthCheckin checkin2 = new TeamHealthCheckin("c1", 3L, WEEK, 1, 1, 1, "");
        WeeklySurvey survey = new WeeklySurvey("c1", 1L, WEEK, "Title", "Desc");

        when(courseRepo.findByProfessorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(course));
        when(courseEnrollmentRepo.findByCourseIdIn(List.of("c1"))).thenReturn(List.of(e1, e2));
        when(weeklySurveyRepo.findByCourseIdInAndWeekStart(List.of("c1"), WEEK))
                .thenReturn(List.of(survey));
        when(teamHealthCheckinRepo.findByCourseIdInAndWeekStart(List.of("c1"), WEEK))
                .thenReturn(List.of(checkin1, checkin2));

        TeamHealthService.ProfessorHealthSummary result
                = teamHealthService.getProfessorWeeklySummary(1L, WEEK);

        assertEquals(2, result.totalSubmissions());
        assertEquals(1, result.atRiskResponses());
        assertEquals(0, result.missingSurveys());
    }

    // --- getStudentWeeklyStatus ---
    @Test
    void getStudentWeeklyStatus_nullStudentId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> teamHealthService.getStudentWeeklyStatus(null, WEEK));
        assertEquals("Student must be logged in.", ex.getMessage());
    }

    @Test
    void getStudentWeeklyStatus_noEnrollments_returnsZeros() {
        when(courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        TeamHealthService.StudentHealthStatus result
                = teamHealthService.getStudentWeeklyStatus(1L, WEEK);

        assertEquals(0, result.enrolledCourses());
        assertEquals(0, result.submittedSurveys());
        assertEquals(0, result.missingSurveys());
    }

    @Test
    void getStudentWeeklyStatus_submittedAllCourses_missingIsZero() {
        CourseEnrollment e1 = new CourseEnrollment("c1", 1L);
        WeeklySurvey survey = new WeeklySurvey("c1", 10L, WEEK, "Title", "Desc");
        TeamHealthCheckin checkin = new TeamHealthCheckin("c1", 1L, WEEK, 4, 4, 4, "");

        when(courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(e1));
        when(weeklySurveyRepo.findByCourseIdInAndWeekStart(List.of("c1"), WEEK))
                .thenReturn(List.of(survey));
        when(teamHealthCheckinRepo.findByStudentIdAndWeekStart(1L, WEEK))
                .thenReturn(List.of(checkin));

        TeamHealthService.StudentHealthStatus result
                = teamHealthService.getStudentWeeklyStatus(1L, WEEK);

        assertEquals(1, result.enrolledCourses());
        assertEquals(1, result.submittedSurveys());
        assertEquals(0, result.missingSurveys());
    }

    @Test
    void getStudentWeeklyStatus_missingSubmission_missingIsOne() {
        CourseEnrollment e1 = new CourseEnrollment("c1", 1L);
        WeeklySurvey survey = new WeeklySurvey("c1", 10L, WEEK, "Title", "Desc");

        when(courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(e1));
        when(weeklySurveyRepo.findByCourseIdInAndWeekStart(List.of("c1"), WEEK))
                .thenReturn(List.of(survey));
        when(teamHealthCheckinRepo.findByStudentIdAndWeekStart(1L, WEEK))
                .thenReturn(List.of());

        TeamHealthService.StudentHealthStatus result
                = teamHealthService.getStudentWeeklyStatus(1L, WEEK);

        assertEquals(1, result.enrolledCourses());
        assertEquals(0, result.submittedSurveys());
        assertEquals(1, result.missingSurveys());
    }
}
