package com.studypilot.studypilot.DomainModel;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "team_health_checkins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id", "week_start"})
)
public class TeamHealthCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "health_score", nullable = false)
    private int healthScore;

    @Column(name = "workload_score", nullable = false)
    private int workloadScore;

    @Column(name = "collaboration_score", nullable = false)
    private int collaborationScore;

    @Column(name = "status_text", length = 600)
    private String statusText;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public TeamHealthCheckin() {
    }

    public TeamHealthCheckin(String courseId,
            Long studentId,
            LocalDate weekStart,
            int healthScore,
            int workloadScore,
            int collaborationScore,
            String statusText) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.weekStart = weekStart;
        this.healthScore = healthScore;
        this.workloadScore = workloadScore;
        this.collaborationScore = collaborationScore;
        this.statusText = statusText;
    }

    @PrePersist
    @PreUpdate
    public void onSave() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public int getWorkloadScore() {
        return workloadScore;
    }

    public int getCollaborationScore() {
        return collaborationScore;
    }

    public String getStatusText() {
        return statusText;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public void setWorkloadScore(int workloadScore) {
        this.workloadScore = workloadScore;
    }

    public void setCollaborationScore(int collaborationScore) {
        this.collaborationScore = collaborationScore;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
