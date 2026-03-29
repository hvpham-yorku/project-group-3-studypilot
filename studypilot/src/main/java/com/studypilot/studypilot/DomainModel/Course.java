package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
/**
 * Course component.
 */
public class Course {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String id;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "join_code", unique = true, length = 8)
    private String joinCode;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Course() {
    }

    public Course(String id, String courseCode, String courseName, Long professorId) {
        this(id, courseCode, courseName, professorId, null);
    }

    public Course(String id, String courseCode, String courseName, Long professorId, String joinCode) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.professorId = professorId;
        this.joinCode = joinCode;
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

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}
