package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;

/**
 * GroupFormationSkillOptionRepo component.
 */
public interface GroupFormationSkillOptionRepo extends JpaRepository<GroupFormationSkillOption, Long> {

    List<GroupFormationSkillOption> findByActivityIdOrderByOptionOrderAsc(Long activityId);

    @Modifying
    @Query("delete from GroupFormationSkillOption s where s.activityId = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}
