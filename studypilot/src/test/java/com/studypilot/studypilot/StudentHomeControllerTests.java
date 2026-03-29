package com.studypilot.studypilot;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.Model;

import com.studypilot.studypilot.BusinessLogicLayer.QuizService;
import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.GUILayer.StudentHomeController;

import jakarta.servlet.http.HttpSession;

class StudentHomeControllerTests {

    private StudentHomeController controller;
    private StudentPortalService mockStudentPortalService;
    private QuizService mockQuizService;
    private TeamHealthService mockTeamHealthService;
    private UserRepo mockUserRepo;
    private HttpSession mockSession;
    private Model mockModel;

    @BeforeEach
    void setup() {
        mockStudentPortalService = mock(StudentPortalService.class);
        mockQuizService = mock(QuizService.class);
        mockTeamHealthService = mock(TeamHealthService.class);
        mockUserRepo = mock(UserRepo.class);
        controller = new StudentHomeController(mockStudentPortalService, mockQuizService, mockTeamHealthService, mockUserRepo);
        mockSession = mock(HttpSession.class);
        mockModel = mock(Model.class);
    }

    @Test
    void testStudentHomeWithStudentRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("STUDENT");
        when(mockSession.getAttribute("fullName")).thenReturn("Jane Doe");
        when(mockSession.getAttribute("userId")).thenReturn(1L);
        when(mockStudentPortalService.getStudentCourses(1L)).thenReturn(Collections.emptyList());
        when(mockStudentPortalService.getCoursesAvailableToJoin(1L)).thenReturn(Collections.emptyList());

        // Act
        String view = controller.studentHome(mockSession, mockModel);

        // Assert
        assertEquals("student_home", view);
        verify(mockModel).addAttribute("fullName", "Jane Doe");
    }

    @Test
    void testStudentHomeWithNonStudentRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("PROFESSOR");

        // Act
        String view = controller.studentHome(mockSession, mockModel);

        // Assert
        assertEquals("redirect:/login", view);
        verify(mockModel, never()).addAttribute(anyString(), any());
    }

    @Test
    void testStudentHomeWithNoRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn(null);

        // Act
        String view = controller.studentHome(mockSession, mockModel);

        // Assert
        assertEquals("redirect:/login", view);
        verify(mockModel, never()).addAttribute(anyString(), any());
    }
}
