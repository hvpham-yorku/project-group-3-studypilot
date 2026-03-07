package com.studypilot.studypilot.DataAccessLayer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.StudentGroupPreference;

public interface StudentGroupPreferenceRepo extends JpaRepository<StudentGroupPreference, Long> {

    Optional<StudentGroupPreference> findByActivityIdAndStudentId(Long activityId, Long studentId);
}
