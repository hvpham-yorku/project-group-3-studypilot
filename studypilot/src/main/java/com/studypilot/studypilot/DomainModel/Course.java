package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String id;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Course() {
    }

    public Course(String id, String courseCode, String courseName, Long professorId) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.professorId = professorId;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public Long getProfessorId() {
        return professorId;
    }
}
