package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProfessorHomeControllerTests {

    private ProfessorHomeController controller;
    private HttpSession mockSession;
    private Model mockModel;

    @BeforeEach
    void setup() {
        controller = new ProfessorHomeController();
        mockSession = mock(HttpSession.class);
        mockModel = mock(Model.class);
    }

    @Test
    void testProfHomeWithProfessorRole() {
        // Arrange
        when(mockSession.getAttribute("role")).thenReturn("PROFESSOR");
        when(mockSession.getAttribute("fullName")).thenReturn("Dr. Smith");

        // Act
        String view = controller.profHome(mockSession, mockModel);

        // Assert
        assertEquals("professor_home", view);
        verify(mockModel).addAttribute("fullName", "Dr. Smith");
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
    }
}
