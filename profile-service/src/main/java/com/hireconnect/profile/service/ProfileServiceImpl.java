package com.hireconnect.profile.service;

import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.RecruiterProfile;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CandidateProfileRepository candidateRepo;
    private final RecruiterProfileRepository recruiterRepo;
    private final com.hireconnect.profile.util.ResumeParser resumeParser;

    // ── Candidate ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CandidateProfile addCandidateProfile(CandidateProfile profile) {
        if (candidateRepo.existsByUserId(profile.getUserId())) {
            throw new IllegalStateException("Profile already exists for userId: " + profile.getUserId());
        }
        
        if (profile.getResumeUrl() != null) {
            resumeParser.parseAndFill(profile, profile.getResumeUrl());
        }
        
        return candidateRepo.save(profile);
    }

    @Override
    public CandidateProfile getCandidateById(int profileId) {
        return candidateRepo.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found: " + profileId));
    }

    @Override
    public CandidateProfile getCandidateByUserId(int userId) {
        return candidateRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found for userId: " + userId));
    }

    @Override
    public CandidateProfile getCandidateByEmail(String email) {
        return candidateRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with email: " + email));
    }

    @Override
    @Transactional
    public CandidateProfile updateCandidateProfile(int profileId, CandidateProfile update) {
        CandidateProfile existing = getCandidateById(profileId);
        update.setProfileId(existing.getProfileId());
        update.setUserId(existing.getUserId());
        return candidateRepo.save(update);
    }

    @Override
    @Transactional
    public void deleteCandidateProfile(int profileId) {
        candidateRepo.deleteById(profileId);
    }

    @Override
    public List<CandidateProfile> getAllCandidates() {
        return candidateRepo.findAll();
    }

    // ── Recruiter ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RecruiterProfile addRecruiterProfile(RecruiterProfile profile) {
        if (recruiterRepo.existsByUserId(profile.getUserId())) {
            throw new IllegalStateException("Profile already exists for userId: " + profile.getUserId());
        }
        return recruiterRepo.save(profile);
    }

    @Override
    public RecruiterProfile getRecruiterById(int profileId) {
        return recruiterRepo.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter profile not found: " + profileId));
    }

    @Override
    public RecruiterProfile getRecruiterByUserId(int userId) {
        return recruiterRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter profile not found for userId: " + userId));
    }

    @Override
    public RecruiterProfile getRecruiterByEmail(String email) {
        return recruiterRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found with email: " + email));
    }

    @Override
    @Transactional
    public RecruiterProfile updateRecruiterProfile(int profileId, RecruiterProfile update) {
        RecruiterProfile existing = getRecruiterById(profileId);
        update.setProfileId(existing.getProfileId());
        update.setUserId(existing.getUserId());
        return recruiterRepo.save(update);
    }

    @Override
    @Transactional
    public void deleteRecruiterProfile(int profileId) {
        recruiterRepo.deleteById(profileId);
    }

    @Override
    public List<RecruiterProfile> getAllRecruiters() {
        return recruiterRepo.findAll();
    }
}
