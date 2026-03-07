package com.studypilot.studypilot.GUILayer;

public class CreateGroupFormationForm {

    private String activityName;
    private Integer preferredGroupSize;
    private Integer minTeamSize;
    private Integer maxTeamSize;

    private String topic1;
    private String topic2;
    private String topic3;
    private String topic4;
    private String topic5;

    private String skill1;
    private String skill2;
    private String skill3;
    private String skill4;
    private String skill5;

    private boolean groupTopicsSimilarly = true;
    private boolean groupSkillsSimilarly = false;

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

    public String getTopic1() { return topic1; }
    public void setTopic1(String topic1) { this.topic1 = topic1; }

    public String getTopic2() { return topic2; }
    public void setTopic2(String topic2) { this.topic2 = topic2; }

    public String getTopic3() { return topic3; }
    public void setTopic3(String topic3) { this.topic3 = topic3; }

    public String getTopic4() { return topic4; }
    public void setTopic4(String topic4) { this.topic4 = topic4; }

    public String getTopic5() { return topic5; }
    public void setTopic5(String topic5) { this.topic5 = topic5; }

    public String getSkill1() { return skill1; }
    public void setSkill1(String skill1) { this.skill1 = skill1; }

    public String getSkill2() { return skill2; }
    public void setSkill2(String skill2) { this.skill2 = skill2; }

    public String getSkill3() { return skill3; }
    public void setSkill3(String skill3) { this.skill3 = skill3; }

    public String getSkill4() { return skill4; }
    public void setSkill4(String skill4) { this.skill4 = skill4; }

    public String getSkill5() { return skill5; }
    public void setSkill5(String skill5) { this.skill5 = skill5; }

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
}