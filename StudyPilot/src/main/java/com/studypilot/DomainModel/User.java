package com.studypilot.studypilot;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=320)
    private String email;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Column(nullable=false)
    private String role; // STUDENT or PROFESSOR

    @Column(name="full_name", nullable=false)
    private String fullName;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    public User() {}

    public User(String email, String passwordHash, String role, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
}
