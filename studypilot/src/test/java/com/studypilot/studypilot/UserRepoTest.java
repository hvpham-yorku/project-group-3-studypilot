package com.studypilot.studypilot;

import com.studypilot.studypilot.DomainModel.User;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    void saveAndFindByEmail_returnsUser() {
        // Arrange
        User user = new User("test@example.com", "hash123", "STUDENT", "Test User");
        userRepo.save(user);

        // Act
        Optional<User> found = userRepo.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    void findByEmail_nonexistent_returnsEmpty() {
        Optional<User> found = userRepo.findByEmail("nonexistent@example.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmail_returnsTrueOrFalse() {
        User user = new User("exists@example.com", "hash123", "PROFESSOR", "Exists User");
        userRepo.save(user);

        assertTrue(userRepo.existsByEmail("exists@example.com"));
        assertFalse(userRepo.existsByEmail("notfound@example.com"));
    }
}
