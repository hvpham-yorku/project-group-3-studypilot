package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.QuizQuestion;

public interface QuizQuestionRepo extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizTestIdOrderByQuestionOrderAsc(Long quizTestId);
}
