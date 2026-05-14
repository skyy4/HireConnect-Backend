package com.hireconnect.profile.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a team member who can co-manage job postings
 * under a recruiter's company account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"recruiter_id", "member_user_id", "email"}))
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int teamMemberId;

    /** userId of the recruiter who owns the team (from auth-service) */
    @Column(nullable = false, name = "recruiter_id")
    private int recruiterId;

    /**
     * userId of the invited team member (from auth-service).
     * Set to 0 for external email invitations that haven't been accepted yet.
     */
    @Column(nullable = false, name = "member_user_id")
    @Builder.Default
    private int memberUserId = 0;

    /** Display name — optional on creation, filled when invitation is accepted */
    @Column(length = 100)
    private String fullName;

    @Email
    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Role within the team: ADMIN | VIEWER | MANAGER.
     * Accepts either "teamRole" or "role" from JSON payload.
     */
    @JsonAlias("role")
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String teamRole = "VIEWER";

    /**
     * Invitation status: PENDING | ACCEPTED | REVOKED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime invitedAt = LocalDateTime.now();

    private LocalDateTime acceptedAt;
}
