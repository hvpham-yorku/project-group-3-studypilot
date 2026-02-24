package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StudentHomeControllerTests {

    private StudentHomeController controller;
    private HttpSession mockSession;
    private Model mockModel;

    @BeforeEach
    void setup() {
        controller = new StudentHomeController();
        mockSession = mock(HttpSession.class);
        mockModel = mock(Model.class);
    }

    @Test
    void testStudentHomeWithStudentRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("STUDENT");
        when(mockSession.getAttribute("fullName")).thenReturn("Jane Doe");

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
