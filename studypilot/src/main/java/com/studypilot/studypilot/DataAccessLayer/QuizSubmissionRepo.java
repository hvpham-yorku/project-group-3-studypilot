package com.studypilot.studypilot.DataAccessLayer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.QuizSubmission;

public interface QuizSubmissionRepo extends JpaRepository<QuizSubmission, Long> {

    Optional<QuizSubmission> findTopByQuizTestIdAndStudentIdOrderBySubmittedAtDesc(Long quizTestId, Long studentId);
}
