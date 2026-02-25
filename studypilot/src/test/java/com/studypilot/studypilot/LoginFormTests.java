package com.studypilot.studypilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.studypilot.studypilot.GUILayer.LoginForm;

public class LoginFormTests {

    @Test
    public void testEmailSetterGetter() {
        LoginForm form = new LoginForm();
        form.setEmail("alice@example.com");
        assertEquals("alice@example.com", form.getEmail());
    }

    @Test
    public void testPasswordSetterGetter() {
        LoginForm form = new LoginForm();
        form.setPassword("password123");
        assertEquals("password123", form.getPassword());
    }

    @Test
    public void testDefaultConstructor() {
        LoginForm form = new LoginForm();
        assertNull(form.getEmail());
        assertNull(form.getPassword());
    }
}
