package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_question_options")
public class SurveyQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Column(name = "option_text", nullable = false, length = 150)
    private String optionText;

    public SurveyQuestionOption() {
    }

    public SurveyQuestionOption(Long questionId, int optionOrder, String optionText) {
        this.questionId = questionId;
        this.optionOrder = optionOrder;
        this.optionText = optionText;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public int getOptionOrder() {
        return optionOrder;
    }

    public String getOptionText() {
        return optionText;
    }
}
