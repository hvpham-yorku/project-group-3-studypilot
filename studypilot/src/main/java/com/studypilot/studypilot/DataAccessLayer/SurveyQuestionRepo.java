package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studypilot.studypilot.DomainModel.SurveyQuestion;

public interface SurveyQuestionRepo extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findByActivityIdOrderByQuestionOrderAsc(Long activityId);

    @Modifying
    @Query("delete from SurveyQuestion q where q.activityId = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}
