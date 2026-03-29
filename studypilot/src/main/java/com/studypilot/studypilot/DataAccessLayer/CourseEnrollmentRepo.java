package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.CourseEnrollment;

/**
 * CourseEnrollmentRepo component.
 */
public interface CourseEnrollmentRepo extends JpaRepository<CourseEnrollment, Long> {

    boolean existsByCourseIdAndStudentId(String courseId, Long studentId);

    List<CourseEnrollment> findByCourseId(String courseId);

    List<CourseEnrollment> findByCourseIdIn(List<String> courseIds);

    List<CourseEnrollment> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
