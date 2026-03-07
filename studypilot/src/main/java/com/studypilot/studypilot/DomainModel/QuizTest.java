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
@Table(name = "quiz_tests")
public class QuizTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "source_file_name", nullable = false, length = 255)
    private String sourceFileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public QuizTest() {
    }

    public QuizTest(String courseId, Long professorId, String title, String sourceFileName) {
        this.courseId = courseId;
        this.professorId = professorId;
        this.title = title;
        this.sourceFileName = sourceFileName;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
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

    public String getTitle() {
        return title;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
