package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypilot.studypilot.DomainModel.Availability;

@Repository
public interface AvailabilityRepo extends JpaRepository<Availability, Long> {

    List<Availability> findByStudentIdAndCourseId(Long studentId, String courseId);

    List<Availability> findByStudentIdInAndCourseId(List<Long> studentIds, String courseId);

    void deleteByStudentIdAndCourseId(Long studentId, String courseId);
}