package com.studypilot.studypilot.DataAccessLayer;

import com.studypilot.studypilot.DomainModel.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    void saveAndFindByEmail_returnsUser() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        userRepo.save(user);

        // Act
        Optional<User> found = userRepo.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void findByEmail_nonexistent_returnsEmpty() {
        Optional<User> found = userRepo.findByEmail("nonexistent@example.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmail_returnsTrueOrFalse() {
        User user = new User();
        user.setId(2L);
        user.setEmail("exists@example.com");
        userRepo.save(user);

        assertTrue(userRepo.existsByEmail("exists@example.com"));
        assertFalse(userRepo.existsByEmail("notfound@example.com"));
    }
}