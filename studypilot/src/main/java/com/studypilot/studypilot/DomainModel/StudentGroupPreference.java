package com.studypilot.studypilot.DomainModel;

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
        name = "student_group_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "student_id"})
)
public class StudentGroupPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "topic_choice", nullable = false, length = 120)
    private String topicChoice;

    @Column(name = "skill_choice", nullable = false, length = 120)
    private String skillChoice;

    @Column(name = "notes", length = 600)
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public StudentGroupPreference() {
    }

    public StudentGroupPreference(Long activityId,
            String courseId,
            Long studentId,
            String topicChoice,
            String skillChoice,
            String notes) {
        this.activityId = activityId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.topicChoice = topicChoice;
        this.skillChoice = skillChoice;
        this.notes = notes;
    }

    @PrePersist
    @PreUpdate
    public void onSave() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public String getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getTopicChoice() {
        return topicChoice;
    }

    public String getSkillChoice() {
        return skillChoice;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTopicChoice(String topicChoice) {
        this.topicChoice = topicChoice;
    }

    public void setSkillChoice(String skillChoice) {
        this.skillChoice = skillChoice;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
