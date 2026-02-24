package com.studypilot.studypilot;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {

    @Test
    void testConstructorAndGetters() {
        User user = new User("test@example.com", "hashedPass", "STUDENT", "Test User");

        // Check constructor values
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPass", user.getPasswordHash());
        assertEquals("STUDENT", user.getRole());
        assertEquals("Test User", user.getFullName());

        // ID should be null before persistence
        assertNull(user.getId());
    }

    @Test
    void testPrePersistSetsCreatedAt() {
        User user = new User("a@b.com", "pass", "PROFESSOR", "Alice");

        // Before prePersist, createdAt should be null
        user.prePersist(); 

        // Use reflection to access private createdAt for testing
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            OffsetDateTime createdAt = (OffsetDateTime) field.get(user);
            assertNotNull(createdAt, "createdAt should be set by prePersist");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testNullOrEmptyValues() {
        User user = new User(null, "", "", null);

        assertNull(user.getEmail());
        assertEquals("", user.getPasswordHash());
        assertEquals("", user.getRole());
        assertNull(user.getFullName());
    }
}
