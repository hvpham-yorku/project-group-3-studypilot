package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthenticationControllerTests {

    private Authentication authService;
    private AuthenticationController controller;
    private Model mockModel;
    private HttpSession mockSession;

    @BeforeEach
    void setup() {
        authService = mock(Authentication.class);
        controller = new AuthenticationController(authService);
        mockModel = mock(Model.class);
        mockSession = mock(HttpSession.class);
    }

    @Test
    void testShowRegisterReturnsRegisterView() {
        String view = controller.showRegister(mockModel);
        assertEquals("register", view);
        verify(mockModel).addAttribute(eq("form"), any(RegisterForm.class));
    }

    @Test
    void testShowLoginReturnsLoginView() {
        String view = controller.showLogin(mockModel);
        assertEquals("login", view);
        verify(mockModel).addAttribute(eq("form"), any(LoginForm.class));
    }

    @Test
    void testDoRegisterSuccessStudent() {
        RegisterForm form = new RegisterForm();
        form.setFullName("Alice");
        form.setEmail("alice@example.com");
        form.setPassword("password123");
        form.setRole("STUDENT");

        User mockUser = new User("alice@example.com", "hash", "STUDENT", "Alice");
        when(authService.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockUser);

        String view = controller.doRegister(form, mockModel, mockSession);
        assertEquals("redirect:/student/home", view);

        verify(mockSession).setAttribute("userId", mockUser.getId());
        verify(mockSession).setAttribute("role", mockUser.getRole());
        verify(mockSession).setAttribute("fullName", mockUser.getFullName());
    }

    @Test
    void testDoRegisterThrowsIllegalArgumentException() {
        RegisterForm form = new RegisterForm();
        form.setFullName("Bob");
        form.setEmail("bob@example.com");
        form.setPassword("pass");
        form.setRole("STUDENT");

        when(authService.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid"));

        String view = controller.doRegister(form, mockModel, mockSession);
        assertEquals("register", view);
        verify(mockModel).addAttribute("error", "Invalid");
    }

    @Test
    void testDoLoginSuccessProfessor() {
        LoginForm form = new LoginForm();
        form.setEmail("prof@example.com");
        form.setPassword("password");

        User mockUser = new User("prof@example.com", "hash", "PROFESSOR", "Prof");
        when(authService.login(anyString(), anyString())).thenReturn(mockUser);

        String view = controller.doLogin(form, mockModel, mockSession);
        assertEquals("redirect:/prof/home", view);

        verify(mockSession).setAttribute("userId", mockUser.getId());
        verify(mockSession).setAttribute("role", mockUser.getRole());
        verify(mockSession).setAttribute("fullName", mockUser.getFullName());
    }

    @Test
    void testDoLoginThrowsIllegalArgumentException() {
        LoginForm form = new LoginForm();
        form.setEmail("wrong@example.com");
        form.setPassword("badpass");

        when(authService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid email or password."));

        String view = controller.doLogin(form, mockModel, mockSession);
        assertEquals("login", view);
        verify(mockModel).addAttribute("error", "Invalid email or password.");
    }

    @Test
    void testLogoutInvalidatesSession() {
        String view = controller.logout(mockSession);
        assertEquals("redirect:/", view);
        verify(mockSession).invalidate();
    }
}
