package com.studypilot.studypilot;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.Model;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.GUILayer.ProfessorHomeController;

import jakarta.servlet.http.HttpSession;

/**
 * ProfessorHomeControllerTests component.
 */
class ProfessorHomeControllerTests {

    private ProfessorHomeController controller;
    private CourseService mockCourseService;
    private TeamHealthService mockTeamHealthService;
    private HttpSession mockSession;
    private Model mockModel;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockCourseService = mock(CourseService.class);
        mockTeamHealthService = mock(TeamHealthService.class);
        CourseEnrollmentRepo mockEnrollmentRepo = mock(CourseEnrollmentRepo.class);
        UserRepo mockUserRepo = mock(UserRepo.class);
        controller = new ProfessorHomeController(
                mockCourseService,
                mockTeamHealthService,
                mockEnrollmentRepo,
                mockUserRepo);
        mockSession = mock(HttpSession.class);
        mockModel = mock(Model.class);
    }

    @Test
    void testProfHomeWithProfessorRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("PROFESSOR");
        when(mockSession.getAttribute("userId")).thenReturn(1L);
        when(mockSession.getAttribute("fullName")).thenReturn("Dr. Smith");
        when(mockTeamHealthService.getProfessorWeeklySummary(eq(1L), any())).thenReturn(
                new TeamHealthService.ProfessorHealthSummary(java.time.LocalDate.now(), 0, 0, 0, 0));
        when(mockTeamHealthService.getProfessorHealthTrend(eq(1L), any(), eq(6))).thenReturn(
                new TeamHealthService.ProfessorHealthTrend(java.util.List.of(), "", "No survey submissions yet."));
        when(mockTeamHealthService.getWeekStart(any())).thenReturn(java.time.LocalDate.now());
        when(mockCourseService.getCoursesForProfessor(1L)).thenReturn(Collections.emptyList());

        // Act
        String view = controller.profHome(mockSession, mockModel);

        // Assert
        assertEquals("professor_home", view);
        verify(mockModel).addAttribute("fullName", "Dr. Smith");
        verify(mockModel).addAttribute(eq("form"), any());
        verify(mockModel).addAttribute("courses", Collections.emptyList());
        verify(mockCourseService).getCoursesForProfessor(1L);
    }

    @Test
    void testProfHomeWithNonProfessorRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("STUDENT");

        // Act
        String view = controller.profHome(mockSession, mockModel);

        // Assert
        assertEquals("redirect:/login", view);
        verify(mockModel, never()).addAttribute(anyString(), any());
        verify(mockCourseService, never()).getCoursesForProfessor(anyLong());
    }

    @Test
    void testProfHomeWithNoRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn(null);

        // Act
        String view = controller.profHome(mockSession, mockModel);

        // Assert
        assertEquals("redirect:/login", view);
        verify(mockModel, never()).addAttribute(anyString(), any());
        verify(mockCourseService, never()).getCoursesForProfessor(anyLong());
    }
}
