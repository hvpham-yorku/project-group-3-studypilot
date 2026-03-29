package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "student_availability")
/**
 * Availability component.
 */
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "time_slot", nullable = false, length = 50)
    private String timeSlot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Availability() {}

    public Availability(Long studentId, String courseId, String timeSlot) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.timeSlot = timeSlot;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getTimeSlot() { return timeSlot; }

    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}
