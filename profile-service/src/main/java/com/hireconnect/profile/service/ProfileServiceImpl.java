package com.hireconnect.profile.service;

import com.hireconnect.profile.pojo.*;
import com.hireconnect.profile.repository.*;
import com.hireconnect.profile.util.ResumeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CandidateProfileRepository candidateRepo;
    private final RecruiterProfileRepository recruiterRepo;
    private final ParsedResumeRepository parsedResumeRepo;
    private final TeamMemberRepository teamMemberRepo;
    private final ResumeParser resumeParser;

    // ── Candidate ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CandidateProfile addCandidateProfile(CandidateProfile profile) {
        log.info("Creating candidate profile for userId={}", profile.getUserId());
        if (candidateRepo.existsByUserId(profile.getUserId())) {
            log.warn("Duplicate candidate profile attempt for userId={}", profile.getUserId());
            throw new IllegalStateException("Profile already exists for userId: " + profile.getUserId());
        }
        CandidateProfile saved = candidateRepo.save(profile);
        log.info("Candidate profile created: profileId={}", saved.getProfileId());
        // Trigger resume parsing immediately on profile creation if resumeUrl present
        if (saved.getResumeUrl() != null && !saved.getResumeUrl().isBlank()) {
            parseAndSaveResume(saved.getProfileId(), saved.getResumeUrl());
        }
        return saved;
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
        CandidateProfile saved = candidateRepo.save(update);
        // Re-parse resume if URL changed
        if (saved.getResumeUrl() != null && !saved.getResumeUrl().isBlank()) {
            parseAndSaveResume(saved.getProfileId(), saved.getResumeUrl());
        }
        return saved;
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

    // ── Resume Parsing ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ParsedResume uploadCandidateResume(int candidateProfileId, MultipartFile file) {
        log.info("Resume upload for profileId={} fileName={}", candidateProfileId, file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            log.warn("Invalid resume content type: {}", file.getContentType());
            throw new IllegalArgumentException("Only PDF resumes are supported");
        }

        CandidateProfile profile = getCandidateById(candidateProfileId);
        Path uploadDir = Paths.get("uploads", "resumes");
        try {
            Files.createDirectories(uploadDir);
            String fileOriginalName = file.getOriginalFilename();
            String originalName = fileOriginalName != null ? fileOriginalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "resume.pdf";
            String storedName = UUID.randomUUID() + "-" + originalName;
            Path target = uploadDir.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/uploads/resumes/" + storedName;
            profile.setResumeUrl(publicUrl);
            candidateRepo.save(profile);
            return parseAndSaveResume(candidateProfileId, publicUrl);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded resume", e);
        }
    }

    @Override
    @Transactional
    public ParsedResume parseAndSaveResume(int candidateProfileId, String resumeUrl) {
        log.info("Parsing resume for profileId={} url={}", candidateProfileId, resumeUrl);
        // Retrieve or create the ParsedResume record
        ParsedResume parsed = parsedResumeRepo.findByCandidateProfileId(candidateProfileId)
                .orElse(ParsedResume.builder()
                        .candidateProfileId(candidateProfileId)
                        .build());

        parsed.setResumeUrl(resumeUrl);
        parsed.setParseStatus("PENDING");

        // Delegate extraction to the ResumeParser utility
        resumeParser.parse(parsed, resumeUrl);

        parsed.setParseStatus("COMPLETED");
        parsed.setUpdatedAt(LocalDateTime.now());
        ParsedResume saved = parsedResumeRepo.save(parsed);

        // Back-fill skills onto the CandidateProfile if profile exists
        candidateRepo.findById(candidateProfileId).ifPresent(profile -> {
            List<String> current = profile.getSkills();
            parsed.getExtractedSkills().forEach(s -> {
                if (!current.contains(s)) current.add(s);
            });
            profile.setSkills(current);
            if ((profile.getBio() == null || profile.getBio().isBlank()) && parsed.getSummary() != null) {
                profile.setBio(parsed.getSummary());
            }
            candidateRepo.save(profile);
        });

        return saved;
    }

    @Override
    public ParsedResume getParsedResume(int candidateProfileId) {
        return parsedResumeRepo.findByCandidateProfileId(candidateProfileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No parsed resume found for profile: " + candidateProfileId));
    }

    // ── Team Member Management ────────────────────────────────────────────

    @Override
    @Transactional
    public TeamMember inviteTeamMember(int recruiterId, TeamMember member) {
        // Check by email since memberUserId is 0 for external e-mail invites
        boolean alreadyInvited = teamMemberRepo.findByRecruiterId(recruiterId)
                .stream()
                .anyMatch(m -> m.getEmail() != null && m.getEmail().equalsIgnoreCase(member.getEmail())
                               && !"REVOKED".equals(m.getStatus()));
        if (alreadyInvited) {
            throw new IllegalStateException(
                    "Team member with email " + member.getEmail() + " is already invited.");
        }
        member.setRecruiterId(recruiterId);
        member.setStatus("PENDING");
        member.setInvitedAt(java.time.LocalDateTime.now());
        if (member.getTeamRole() == null || member.getTeamRole().isBlank()) {
            member.setTeamRole("VIEWER");
        }
        return teamMemberRepo.save(member);
    }

    @Override
    @Transactional
    public TeamMember acceptInvitation(int recruiterId, int memberUserId) {
        TeamMember member = teamMemberRepo.findByRecruiterIdAndMemberUserId(recruiterId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invitation not found for recruiterId=" + recruiterId + " memberUserId=" + memberUserId));
        if (!"PENDING".equals(member.getStatus())) {
            throw new IllegalStateException("Invitation is not in PENDING state: " + member.getStatus());
        }
        member.setStatus("ACCEPTED");
        member.setAcceptedAt(LocalDateTime.now());
        return teamMemberRepo.save(member);
    }

    @Override
    @Transactional
    public TeamMember updateTeamMemberRole(int recruiterId, int memberUserId, String newRole) {
        TeamMember member = teamMemberRepo.findByRecruiterIdAndMemberUserId(recruiterId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));
        member.setTeamRole(newRole);
        return teamMemberRepo.save(member);
    }

    @Override
    @Transactional
    public void revokeTeamMember(int recruiterId, int teamMemberId) {
        TeamMember member = teamMemberRepo.findById(teamMemberId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));
        if (member.getRecruiterId() != recruiterId) {
             throw new IllegalArgumentException("Team member does not belong to this recruiter");
        }
        member.setStatus("REVOKED");
        teamMemberRepo.save(member);
    }

    @Override
    public List<TeamMember> getTeamMembers(int recruiterId) {
        return teamMemberRepo.findByRecruiterId(recruiterId);
    }

    @Override
    public List<TeamMember> getTeamMembersByStatus(int recruiterId, String status) {
        return teamMemberRepo.findByRecruiterIdAndStatus(recruiterId, status);
    }

    @Override
    public boolean isTeamMember(int recruiterId, int memberUserId) {
        return teamMemberRepo.existsByRecruiterIdAndMemberUserId(recruiterId, memberUserId);
    }
}
