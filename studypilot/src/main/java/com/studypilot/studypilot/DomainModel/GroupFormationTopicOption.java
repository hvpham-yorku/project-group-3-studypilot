package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;

@Entity
@Table(name = "group_formation_topic_options")
public class GroupFormationTopicOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Column(name = "topic_text", nullable = false, length = 120)
    private String topicText;

    public GroupFormationTopicOption() {
    }

    public GroupFormationTopicOption(Long activityId, int optionOrder, String topicText) {
        this.activityId = activityId;
        this.optionOrder = optionOrder;
        this.topicText = topicText;
    }

    public Long getId() {
        return id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public int getOptionOrder() {
        return optionOrder;
    }

    public String getTopicText() {
        return topicText;
    }
}