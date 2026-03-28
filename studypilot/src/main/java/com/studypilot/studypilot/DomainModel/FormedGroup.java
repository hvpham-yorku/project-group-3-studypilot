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
@Table(name = "formed_groups")
public class FormedGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "group_number", nullable = false)
    private int groupNumber;

    @Column(name = "group_name", nullable = false, length = 150)
    private String groupName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public FormedGroup() {
    }

    public FormedGroup(Long activityId, String courseId, int groupNumber, String groupName) {
        this.activityId = activityId;
        this.courseId = courseId;
        this.groupNumber = groupNumber;
        this.groupName = groupName;
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

    public Long getActivityId() {
        return activityId;
    }

    public String getCourseId() {
        return courseId;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public String getGroupName() {
        return groupName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
