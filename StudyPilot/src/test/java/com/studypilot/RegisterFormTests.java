package com.studypilot.studypilot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterFormTests {

    @Test
    public void testFullNameSetterGetter() {
        RegisterForm form = new RegisterForm();
        form.setFullName("Alice Wonderland");
        assertEquals("Alice Wonderland", form.getFullName());
    }

    @Test
    public void testEmailSetterGetter() {
        RegisterForm form = new RegisterForm();
        form.setEmail("alice@example.com");
        assertEquals("alice@example.com", form.getEmail());
    }

    @Test
    public void testPasswordSetterGetter() {
        RegisterForm form = new RegisterForm();
        form.setPassword("password123");
        assertEquals("password123", form.getPassword());
    }

    @Test
    public void testRoleSetterGetter() {
        RegisterForm form = new RegisterForm();
        form.setRole("STUDENT");
        assertEquals("STUDENT", form.getRole());
    }

    @Test
    public void testDefaultConstructor() {
        RegisterForm form = new RegisterForm();
        assertNull(form.getFullName());
        assertNull(form.getEmail());
        assertNull(form.getPassword());
        assertNull(form.getRole());
    }
}

