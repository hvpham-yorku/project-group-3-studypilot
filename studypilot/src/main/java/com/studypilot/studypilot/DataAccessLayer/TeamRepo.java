package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypilot.studypilot.DomainModel.Team;

@Repository
/**
 * TeamRepo component.
 */
public interface TeamRepo extends JpaRepository<Team, Long> {

    List<Team> findByCourseIdOrderByIdAsc(String courseId);

}
