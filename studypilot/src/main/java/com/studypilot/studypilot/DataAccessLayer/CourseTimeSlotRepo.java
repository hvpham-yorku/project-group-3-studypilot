package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypilot.studypilot.DomainModel.CourseTimeSlot;

@Repository
/**
 * CourseTimeSlotRepo component.
 */
public interface CourseTimeSlotRepo extends JpaRepository<CourseTimeSlot, Long> {

    // Fetches all published meeting slot options for one course.
    List<CourseTimeSlot> findByCourseId(String courseId);

    // Clears previously published slot options before replacing them.
    void deleteByCourseId(String courseId);

}
