package com.studypilot.studypilot.DataAccessLayer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.WeeklySurvey;

public interface WeeklySurveyRepo extends JpaRepository<WeeklySurvey, Long> {

    Optional<WeeklySurvey> findByCourseIdAndWeekStart(String courseId, LocalDate weekStart);

    boolean existsByCourseIdAndWeekStart(String courseId, LocalDate weekStart);

    List<WeeklySurvey> findByCourseIdInAndWeekStart(List<String> courseIds, LocalDate weekStart);
}
