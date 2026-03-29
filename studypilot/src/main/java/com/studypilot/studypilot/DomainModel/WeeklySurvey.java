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
        name = "weekly_surveys",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "week_start"})
)
/**
 * WeeklySurvey component.
 */
public class WeeklySurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public WeeklySurvey() {
    }

    public WeeklySurvey(String courseId,
            Long professorId,
            LocalDate weekStart,
            String title,
            String description) {
        this.courseId = courseId;
        this.professorId = professorId;
        this.weekStart = weekStart;
        this.title = title;
        this.description = description;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
