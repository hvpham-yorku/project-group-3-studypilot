package com.studypilot.studypilot.DomainModel;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "group_formation_activities")
public class GroupFormationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "activity_name", nullable = false, length = 150)
    private String activityName;

    @Column(name = "preferred_group_size", nullable = false)
    private int preferredGroupSize;

    @Column(name = "min_team_size", nullable = false)
    private int minTeamSize;

    @Column(name = "max_team_size", nullable = false)
    private int maxTeamSize;

    @Column(name = "group_topics_similarly", nullable = false)
    private boolean groupTopicsSimilarly;

    @Column(name = "group_skills_similarly", nullable = false)
    private boolean groupSkillsSimilarly;

    @Column(name = "status", length = 20, columnDefinition = "varchar(20) default 'OPEN'")
    private String status = "OPEN";

    @Column(name = "deadline")
    private OffsetDateTime deadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public GroupFormationActivity() {
    }

    public GroupFormationActivity(
            String courseId,
            Long professorId,
            String activityName,
            int preferredGroupSize,
            int minTeamSize,
            int maxTeamSize,
            boolean groupTopicsSimilarly,
            boolean groupSkillsSimilarly) {
        this.courseId = courseId;
        this.professorId = professorId;
        this.activityName = activityName;
        this.preferredGroupSize = preferredGroupSize;
        this.minTeamSize = minTeamSize;
        this.maxTeamSize = maxTeamSize;
        this.groupTopicsSimilarly = groupTopicsSimilarly;
        this.groupSkillsSimilarly = groupSkillsSimilarly;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = "OPEN";
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

    public String getActivityName() {
        return activityName;
    }

    public int getPreferredGroupSize() {
        return preferredGroupSize;
    }

    public int getMinTeamSize() {
        return minTeamSize;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public boolean isGroupTopicsSimilarly() {
        return groupTopicsSimilarly;
    }

    public boolean isGroupSkillsSimilarly() {
        return groupSkillsSimilarly;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setPreferredGroupSize(int preferredGroupSize) {
        this.preferredGroupSize = preferredGroupSize;
    }

    public void setMinTeamSize(int minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public void setGroupTopicsSimilarly(boolean groupTopicsSimilarly) {
        this.groupTopicsSimilarly = groupTopicsSimilarly;
    }

    public void setGroupSkillsSimilarly(boolean groupSkillsSimilarly) {
        this.groupSkillsSimilarly = groupSkillsSimilarly;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(OffsetDateTime deadline) {
        this.deadline = deadline;
    }
}