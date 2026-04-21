package com.hireconnect.profile.service;

import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.RecruiterProfile;

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
}
