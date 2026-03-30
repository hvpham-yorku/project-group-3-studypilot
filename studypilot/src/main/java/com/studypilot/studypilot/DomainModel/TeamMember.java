package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "team_members")
/**
 * TeamMember component.
 */
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private final Instant createdAt = Instant.now();

    public TeamMember() {
    }

    public TeamMember(Long teamId, Long studentId) {
        this.teamId = teamId;
        this.studentId = studentId;
    }

    public Long getId() {
        return id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
