package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.studypilot.studypilot.DomainModel.FormedGroup;

public interface FormedGroupRepo extends JpaRepository<FormedGroup, Long> {

    List<FormedGroup> findByActivityIdOrderByGroupNumberAsc(Long activityId);

    List<FormedGroup> findByCourseId(String courseId);

    @Modifying
    @Query("DELETE FROM FormedGroup g WHERE g.activityId = :activityId")
    void deleteByActivityId(Long activityId);
}
