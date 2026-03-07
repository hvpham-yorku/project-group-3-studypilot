package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypilot.studypilot.DomainModel.Course;

public interface CourseRepo extends JpaRepository<Course, String> {

    List<Course> findByProfessorIdOrderByCreatedAtDesc(Long professorId);

    List<Course> findAllByOrderByCreatedAtDesc();
}
