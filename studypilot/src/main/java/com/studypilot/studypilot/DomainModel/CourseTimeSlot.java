package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;

@Entity
@Table(name = "course_time_slots")
public class CourseTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "slot_label", nullable = false, length = 50)
    private String slotLabel; 
    
    public CourseTimeSlot() {}

    public CourseTimeSlot(String courseId, String slotLabel) {
        this.courseId = courseId;
        this.slotLabel = slotLabel;
    }

    public Long getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getSlotLabel() { return slotLabel; }

    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setSlotLabel(String slotLabel) { this.slotLabel = slotLabel; }
}