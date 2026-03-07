package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_test_id", nullable = false)
    private Long quizTestId;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    public QuizSubmission() {
    }

    public QuizSubmission(Long quizTestId, String courseId, Long studentId, int score, int totalQuestions) {
        this.quizTestId = quizTestId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.score = score;
        this.totalQuestions = totalQuestions;
    }

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) {
            submittedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getQuizTestId() {
        return quizTestId;
    }

    public String getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}
