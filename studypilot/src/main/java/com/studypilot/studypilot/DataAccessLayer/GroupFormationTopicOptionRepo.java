package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;

/**
 * GroupFormationTopicOptionRepo component.
 */
public interface GroupFormationTopicOptionRepo extends JpaRepository<GroupFormationTopicOption, Long> {

    List<GroupFormationTopicOption> findByActivityIdOrderByOptionOrderAsc(Long activityId);

    @Modifying
    @Query("delete from GroupFormationTopicOption t where t.activityId = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}
