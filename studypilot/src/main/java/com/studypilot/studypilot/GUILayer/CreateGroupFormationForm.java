package com.studypilot.studypilot.GUILayer;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFormationForm {

    private String activityName;
    private Integer preferredGroupSize;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private String deadline;
    private List<QuestionForm> questions = new ArrayList<>();

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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<QuestionForm> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionForm> questions) {
        this.questions = questions;
    }

    public static class QuestionForm {

        private String title;
        private String type; // SELECT or RATING
        private String strategy; // SIMILAR or DIVERSE
        private List<String> options = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }
    }
}
