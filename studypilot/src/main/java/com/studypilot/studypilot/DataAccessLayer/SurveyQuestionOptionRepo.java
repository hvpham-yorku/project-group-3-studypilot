package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studypilot.studypilot.DomainModel.SurveyQuestionOption;

public interface SurveyQuestionOptionRepo extends JpaRepository<SurveyQuestionOption, Long> {

    List<SurveyQuestionOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    List<SurveyQuestionOption> findByQuestionIdIn(List<Long> questionIds);

    @Modifying
    @Query("delete from SurveyQuestionOption o where o.questionId in :questionIds")
    void deleteByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}
