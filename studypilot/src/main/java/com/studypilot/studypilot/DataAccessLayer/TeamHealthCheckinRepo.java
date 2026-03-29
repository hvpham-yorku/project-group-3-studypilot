package com.studypilot.studypilot.DataAccessLayer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;

public interface TeamHealthCheckinRepo extends JpaRepository<TeamHealthCheckin, Long> {

    Optional<TeamHealthCheckin> findByCourseIdAndStudentIdAndWeekStart(String courseId, Long studentId, LocalDate weekStart);

    List<TeamHealthCheckin> findByCourseIdInAndWeekStart(List<String> courseIds, LocalDate weekStart);

    List<TeamHealthCheckin> findByCourseIdInAndWeekStartBetween(List<String> courseIds, LocalDate startWeek, LocalDate endWeek);

    List<TeamHealthCheckin> findByStudentIdAndWeekStart(Long studentId, LocalDate weekStart);

    List<TeamHealthCheckin> findByStudentIdAndCourseIdInAndWeekStart(Long studentId, List<String> courseIds, LocalDate weekStart);
    
    List<TeamHealthCheckin> findByStudentIdOrderByWeekStartDesc(Long studentId);
}
