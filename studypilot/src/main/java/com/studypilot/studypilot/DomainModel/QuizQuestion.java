package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_test_id", nullable = false)
    private Long quizTestId;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "option_a", nullable = false, length = 255)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 255)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 255)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 255)
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    public QuizQuestion() {
    }

    public QuizQuestion(Long quizTestId,
            int questionOrder,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption) {
        this.quizTestId = quizTestId;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    public Long getId() {
        return id;
    }

    public Long getQuizTestId() {
        return quizTestId;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }
}
