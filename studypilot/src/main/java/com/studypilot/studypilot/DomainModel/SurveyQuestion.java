package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_questions")
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "question_title", nullable = false, length = 200)
    private String questionTitle;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType; // SELECT or RATING

    @Column(name = "grouping_strategy", nullable = false, length = 20)
    private String groupingStrategy; // SIMILAR or DIVERSE

    public SurveyQuestion() {
    }

    public SurveyQuestion(Long activityId, int questionOrder, String questionTitle,
                          String questionType, String groupingStrategy) {
        this.activityId = activityId;
        this.questionOrder = questionOrder;
        this.questionTitle = questionTitle;
        this.questionType = questionType;
        this.groupingStrategy = groupingStrategy;
    }

    public Long getId() {
        return id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getGroupingStrategy() {
        return groupingStrategy;
    }
}
