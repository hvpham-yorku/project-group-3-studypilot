package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studypilot.studypilot.DomainModel.SurveyResponse;

public interface SurveyResponseRepo extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findByActivityIdAndStudentId(Long activityId, Long studentId);

    List<SurveyResponse> findByActivityId(Long activityId);

    Optional<SurveyResponse> findByActivityIdAndStudentIdAndQuestionId(Long activityId, Long studentId, Long questionId);

    long countDistinctStudentIdByActivityId(Long activityId);

    @Modifying
    @Query("delete from SurveyResponse r where r.activityId = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);

    @Query("select distinct r.studentId from SurveyResponse r where r.activityId = :activityId")
    List<Long> findDistinctStudentIdsByActivityId(@Param("activityId") Long activityId);
}
