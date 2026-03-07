package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.GroupFormationActivity;

public interface GroupFormationActivityRepo extends JpaRepository<GroupFormationActivity, Long> {

    List<GroupFormationActivity> findByCourseIdOrderByCreatedAtDesc(String courseId);

    Optional<GroupFormationActivity> findByIdAndCourseId(Long id, String courseId);
}