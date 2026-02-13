package com.studypilot.studypilot;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Authentication {

    private final UserRepo userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Authentication(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String fullName, String email, String rawPassword, String role) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String cleanName = fullName == null ? "" : fullName.trim();

        if (cleanName.isBlank()) throw new IllegalArgumentException("Full name is required.");
        if (cleanEmail.isBlank()) throw new IllegalArgumentException("Email is required.");
        if (rawPassword == null || rawPassword.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (!"STUDENT".equals(role) && !"PROFESSOR".equals(role)) throw new IllegalArgumentException("Role must be STUDENT or PROFESSOR.");
        if (userRepository.existsByEmail(cleanEmail)) throw new IllegalArgumentException("That email is already registered.");

        String hash = encoder.encode(rawPassword);
        return userRepository.save(new User(cleanEmail, hash, role, cleanName));
    }

    public User login(String email, String rawPassword) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (cleanEmail.isBlank()) throw new IllegalArgumentException("Email is required.");
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("Password is required.");

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }
}
