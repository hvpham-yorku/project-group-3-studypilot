package com.studypilot.studypilot.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.studypilot.studypilot.DomainModel.FormedGroupMember;

public interface FormedGroupMemberRepo extends JpaRepository<FormedGroupMember, Long> {

    List<FormedGroupMember> findByFormedGroupId(Long formedGroupId);

    List<FormedGroupMember> findByStudentId(Long studentId);

    List<FormedGroupMember> findByFormedGroupIdIn(List<Long> groupIds);

    @Modifying
    @Query("DELETE FROM FormedGroupMember m WHERE m.formedGroupId IN :groupIds")
    void deleteByFormedGroupIdIn(List<Long> groupIds);

    @Modifying
    @Query("DELETE FROM FormedGroupMember m WHERE m.formedGroupId = :formedGroupId AND m.studentId = :studentId")
    void deleteByFormedGroupIdAndStudentId(Long formedGroupId, Long studentId);
}
