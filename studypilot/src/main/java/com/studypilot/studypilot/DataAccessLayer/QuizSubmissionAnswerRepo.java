package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.QuizSubmissionAnswer;

public interface QuizSubmissionAnswerRepo extends JpaRepository<QuizSubmissionAnswer, Long> {

    List<QuizSubmissionAnswer> findBySubmissionId(Long submissionId);
}
