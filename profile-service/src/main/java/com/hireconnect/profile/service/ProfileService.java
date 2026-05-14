package com.hireconnect.profile.service;

import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.ParsedResume;
import com.hireconnect.profile.pojo.RecruiterProfile;
import com.hireconnect.profile.pojo.TeamMember;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    // ── Candidate ──────────────────────────────────────────────────────────
    CandidateProfile addCandidateProfile(CandidateProfile profile);

    CandidateProfile getCandidateById(int profileId);

    CandidateProfile getCandidateByUserId(int userId);

    CandidateProfile getCandidateByEmail(String email);

    CandidateProfile updateCandidateProfile(int profileId, CandidateProfile profile);

    void deleteCandidateProfile(int profileId);

    List<CandidateProfile> getAllCandidates();

    // ── Recruiter ──────────────────────────────────────────────────────────
    RecruiterProfile addRecruiterProfile(RecruiterProfile profile);

    RecruiterProfile getRecruiterById(int profileId);

    RecruiterProfile getRecruiterByUserId(int userId);

    RecruiterProfile getRecruiterByEmail(String email);

    RecruiterProfile updateRecruiterProfile(int profileId, RecruiterProfile profile);

    void deleteRecruiterProfile(int profileId);

    List<RecruiterProfile> getAllRecruiters();

    // ── Resume Parsing ─────────────────────────────────────────────────────
    ParsedResume uploadCandidateResume(int candidateProfileId, MultipartFile file);

    ParsedResume parseAndSaveResume(int candidateProfileId, String resumeUrl);

    ParsedResume getParsedResume(int candidateProfileId);

    // ── Team Member Management ────────────────────────────────────────────
    TeamMember inviteTeamMember(int recruiterId, TeamMember member);

    TeamMember acceptInvitation(int recruiterId, int memberUserId);

    TeamMember updateTeamMemberRole(int recruiterId, int memberUserId, String newRole);

    void revokeTeamMember(int recruiterId, int teamMemberId);

    List<TeamMember> getTeamMembers(int recruiterId);

    List<TeamMember> getTeamMembersByStatus(int recruiterId, String status);

    boolean isTeamMember(int recruiterId, int memberUserId);
}
