package com.studypilot.studypilot.DomainModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "formed_group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"formed_group_id", "student_id"})
)
public class FormedGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formed_group_id", nullable = false)
    private Long formedGroupId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    public FormedGroupMember() {
    }

    public FormedGroupMember(Long formedGroupId, Long studentId) {
        this.formedGroupId = formedGroupId;
        this.studentId = studentId;
    }

    public Long getId() {
        return id;
    }

    public Long getFormedGroupId() {
        return formedGroupId;
    }

    public Long getStudentId() {
        return studentId;
    }
}
