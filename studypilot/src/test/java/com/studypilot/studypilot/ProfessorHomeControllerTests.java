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
import com.studypilot.studypilot.GUILayer.ProfessorHomeController;

import jakarta.servlet.http.HttpSession;

class ProfessorHomeControllerTests {

    private ProfessorHomeController controller;
    private CourseService mockCourseService;
    private HttpSession mockSession;
    private Model mockModel;

    @BeforeEach
    void setup() {
        mockCourseService = mock(CourseService.class);
        controller = new ProfessorHomeController(mockCourseService);
        mockSession = mock(HttpSession.class);
        mockModel = mock(Model.class);
    }

    @Test
    void testProfHomeWithProfessorRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("PROFESSOR");
        when(mockSession.getAttribute("userId")).thenReturn(1L);
        when(mockSession.getAttribute("fullName")).thenReturn("Dr. Smith");
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
