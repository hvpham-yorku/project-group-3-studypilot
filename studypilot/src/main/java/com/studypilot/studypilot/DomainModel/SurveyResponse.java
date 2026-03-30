package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(
        name = "survey_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "student_id", "question_id"})
)
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "response_value", nullable = false, length = 2000)
    private String responseValue;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SurveyResponse() {
    }

    public SurveyResponse(Long activityId, Long studentId, Long questionId, String responseValue) {
        this.activityId = activityId;
        this.studentId = studentId;
        this.questionId = questionId;
        this.responseValue = responseValue;
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

    public Long getStudentId() {
        return studentId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
