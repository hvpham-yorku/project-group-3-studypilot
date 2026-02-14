package com.studypilot.studypilot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthenticationTests {

    private Authentication auth;
    private UserRepo mockRepo;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        mockRepo = mock(UserRepo.class);
        auth = new Authentication(mockRepo);
        encoder = new BCryptPasswordEncoder();
    }

    // ----- REGISTER TESTS -----
    @Test
    public void testRegisterValidStudent() {
        when(mockRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = auth.register("Alice", "alice@example.com", "password123", "STUDENT");

        assertEquals("alice@example.com", user.getEmail());
        assertEquals("STUDENT", user.getRole());
        assertEquals("Alice", user.getFullName());
        assertTrue(encoder.matches("password123", user.getPasswordHash()));
    }

    @Test
    public void testRegisterBlankNameThrows() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
            auth.register(" ", "bob@example.com", "password123", "STUDENT")
        );
        assertEquals("Full name is required.", e.getMessage());
    }

    @Test
    public void testRegisterShortPasswordThrows() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
            auth.register("Bob", "bob@example.com", "123", "STUDENT")
        );
        assertEquals("Password must be at least 6 characters.", e.getMessage());
    }

    @Test
    public void testRegisterDuplicateEmailThrows() {
        when(mockRepo.existsByEmail("alice@example.com")).thenReturn(true);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
            auth.register("Alice", "alice@example.com", "password123", "STUDENT")
        );
        assertEquals("That email is already registered.", e.getMessage());
    }

    // ----- LOGIN TESTS -----
    @Test
    public void testLoginValid() {
        String hash = encoder.encode("password123");
        User user = new User("alice@example.com", hash, "STUDENT", "Alice");

        when(mockRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        User loggedIn = auth.login("alice@example.com", "password123");
        assertEquals("alice@example.com", loggedIn.getEmail());
        assertEquals("Alice", loggedIn.getFullName());
    }

    @Test
    public void testLoginWrongPasswordThrows() {
        String hash = encoder.encode("password123");
        User user = new User("alice@example.com", hash, "STUDENT", "Alice");

        when(mockRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Exception e = assertThrows(IllegalArgumentException.class, () ->
            auth.login("alice@example.com", "wrongpass")
        );
        assertEquals("Invalid email or password.", e.getMessage());
    }

    @Test
    public void testLoginNonexistentEmailThrows() {
        when(mockRepo.findByEmail("bob@example.com")).thenReturn(Optional.empty());

        Exception e = assertThrows(IllegalArgumentException.class, () ->
            auth.login("bob@example.com", "password123")
        );
        assertEquals("Invalid email or password.", e.getMessage());
    }
}

