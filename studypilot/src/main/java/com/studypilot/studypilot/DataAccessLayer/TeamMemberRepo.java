package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypilot.studypilot.DomainModel.TeamMember;

@Repository
/**
 * TeamMemberRepo component.
 */
public interface TeamMemberRepo extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

}
