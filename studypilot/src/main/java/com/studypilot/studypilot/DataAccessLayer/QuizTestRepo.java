package com.studypilot.studypilot.DataAccessLayer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.QuizTest;

public interface QuizTestRepo extends JpaRepository<QuizTest, Long> {

    Optional<QuizTest> findTopByCourseIdOrderByCreatedAtDesc(String courseId);
}
