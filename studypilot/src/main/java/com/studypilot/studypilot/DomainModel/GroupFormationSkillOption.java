package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;

@Entity
@Table(name = "group_formation_skill_options")
/**
 * GroupFormationSkillOption component.
 */
public class GroupFormationSkillOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Column(name = "skill_text", nullable = false, length = 120)
    private String skillText;

    public GroupFormationSkillOption() {
    }

    public GroupFormationSkillOption(Long activityId, int optionOrder, String skillText) {
        this.activityId = activityId;
        this.optionOrder = optionOrder;
        this.skillText = skillText;
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

    public String getSkillText() {
        return skillText;
    }
}
