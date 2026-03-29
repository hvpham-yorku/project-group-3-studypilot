package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypilot.studypilot.DomainModel.CourseTimeSlot;

@Repository
public interface CourseTimeSlotRepo extends JpaRepository<CourseTimeSlot, Long> {

    List<CourseTimeSlot> findByCourseId(String courseId);

    void deleteByCourseId(String courseId);

}
