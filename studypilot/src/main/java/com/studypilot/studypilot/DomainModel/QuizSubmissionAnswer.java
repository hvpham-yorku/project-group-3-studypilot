package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_submission_answers")
public class QuizSubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_option", nullable = false, length = 1)
    private String selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    public QuizSubmissionAnswer() {
    }

    public QuizSubmissionAnswer(Long submissionId, Long questionId, String selectedOption, boolean correct) {
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public boolean isCorrect() {
        return correct;
    }
}
