package com.studypilot.studypilot.GUILayer;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFormationForm {

    private String activityName;
    private Integer preferredGroupSize;
    private Integer minTeamSize;
    private Integer maxTeamSize;

    private List<String> topics = new ArrayList<>();
    private List<String> skills = new ArrayList<>();

    private boolean groupTopicsSimilarly = true;
    private boolean groupSkillsSimilarly = false;

    private String deadline;

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Integer getPreferredGroupSize() {
        return preferredGroupSize;
    }

    public void setPreferredGroupSize(Integer preferredGroupSize) {
        this.preferredGroupSize = preferredGroupSize;
    }

    public Integer getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(Integer minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public boolean isGroupTopicsSimilarly() {
        return groupTopicsSimilarly;
    }

    public void setGroupTopicsSimilarly(boolean groupTopicsSimilarly) {
        this.groupTopicsSimilarly = groupTopicsSimilarly;
    }

    public boolean isGroupSkillsSimilarly() {
        return groupSkillsSimilarly;
    }

    public void setGroupSkillsSimilarly(boolean groupSkillsSimilarly) {
        this.groupSkillsSimilarly = groupSkillsSimilarly;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
