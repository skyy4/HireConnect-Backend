package com.hireconnect.profile.resource;

import com.hireconnect.profile.pojo.*;
import com.hireconnect.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Service", description = "Candidate profiles, Recruiter profiles, Resume parsing, Team management")
public class ProfileResource {

    private final ProfileService profileService;

    // ── Candidate Endpoints ────────────────────────────────────────────────

    @PostMapping("/candidates")
    @Operation(summary = "Create candidate profile")
    public ResponseEntity<CandidateProfile> createCandidate(@Valid @RequestBody CandidateProfile profile) {
        log.info("POST /profiles/candidates userId={}", profile.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addCandidateProfile(profile));
    }

    @GetMapping("/candidates/{profileId}")
    @Operation(summary = "Get candidate profile by profileId")
    public ResponseEntity<CandidateProfile> getCandidate(@PathVariable int profileId) {
        return ResponseEntity.ok(profileService.getCandidateById(profileId));
    }

    @GetMapping("/candidates/user/{userId}")
    @Operation(summary = "Get candidate profile by userId")
    public ResponseEntity<CandidateProfile> getCandidateByUser(@PathVariable int userId) {
        return ResponseEntity.ok(profileService.getCandidateByUserId(userId));
    }

    @GetMapping("/candidates")
    @Operation(summary = "Get all candidate profiles (Admin)")
    public ResponseEntity<List<CandidateProfile>> getAllCandidates() {
        return ResponseEntity.ok(profileService.getAllCandidates());
    }

    @PutMapping("/candidates/{profileId}")
    @Operation(summary = "Update candidate profile")
    public ResponseEntity<CandidateProfile> updateCandidate(
            @PathVariable int profileId,
            @Valid @RequestBody CandidateProfile profile) {
        return ResponseEntity.ok(profileService.updateCandidateProfile(profileId, profile));
    }

    @DeleteMapping("/candidates/{profileId}")
    @Operation(summary = "Delete candidate profile")
    public ResponseEntity<Map<String, String>> deleteCandidate(@PathVariable int profileId) {
        log.warn("DELETE /profiles/candidates/{}", profileId);
        profileService.deleteCandidateProfile(profileId);
        return ResponseEntity.ok(Map.of("message", "Candidate profile deleted"));
    }

    // ── Recruiter Endpoints ────────────────────────────────────────────────

    @PostMapping("/recruiters")
    @Operation(summary = "Create recruiter profile")
    public ResponseEntity<RecruiterProfile> createRecruiter(@Valid @RequestBody RecruiterProfile profile) {
        log.info("POST /profiles/recruiters userId={}", profile.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addRecruiterProfile(profile));
    }

    @GetMapping("/recruiters/{profileId}")
    @Operation(summary = "Get recruiter profile by profileId")
    public ResponseEntity<RecruiterProfile> getRecruiter(@PathVariable int profileId) {
        return ResponseEntity.ok(profileService.getRecruiterById(profileId));
    }

    @GetMapping("/recruiters/user/{userId}")
    @Operation(summary = "Get recruiter profile by userId")
    public ResponseEntity<RecruiterProfile> getRecruiterByUser(@PathVariable int userId) {
        return ResponseEntity.ok(profileService.getRecruiterByUserId(userId));
    }

    @GetMapping("/recruiters")
    @Operation(summary = "Get all recruiter profiles (Admin)")
    public ResponseEntity<List<RecruiterProfile>> getAllRecruiters() {
        return ResponseEntity.ok(profileService.getAllRecruiters());
    }

    @PutMapping("/recruiters/{profileId}")
    @Operation(summary = "Update recruiter profile")
    public ResponseEntity<RecruiterProfile> updateRecruiter(
            @PathVariable int profileId,
            @Valid @RequestBody RecruiterProfile profile) {
        return ResponseEntity.ok(profileService.updateRecruiterProfile(profileId, profile));
    }

    @DeleteMapping("/recruiters/{profileId}")
    @Operation(summary = "Delete recruiter profile")
    public ResponseEntity<Map<String, String>> deleteRecruiter(@PathVariable int profileId) {
        profileService.deleteRecruiterProfile(profileId);
        return ResponseEntity.ok(Map.of("message", "Recruiter profile deleted"));
    }

    // ── Resume Parsing Endpoints ───────────────────────────────────────────

    @PostMapping("/candidates/{profileId}/resume/parse")
    @Operation(summary = "Trigger resume parsing for a candidate profile")
    public ResponseEntity<ParsedResume> parseResume(
            @PathVariable int profileId,
            @RequestBody Map<String, String> body) {
        String resumeUrl = body.get("resumeUrl");
        if (resumeUrl == null || resumeUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(profileService.parseAndSaveResume(profileId, resumeUrl));
    }

    @GetMapping("/candidates/{profileId}/resume/parsed")
    @Operation(summary = "Get the parsed resume data for a candidate")
    public ResponseEntity<ParsedResume> getParsedResume(@PathVariable int profileId) {
        try {
            return ResponseEntity.ok(profileService.getParsedResume(profileId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/candidates/{profileId}/resume/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload a PDF resume for a candidate and parse it")
    public ResponseEntity<ParsedResume> uploadResume(
            @PathVariable int profileId,
            @RequestPart("file") MultipartFile file) {
        log.info("POST /profiles/candidates/{}/resume/upload fileName={}", profileId, file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.uploadCandidateResume(profileId, file));
    }

    // ── Team Member Endpoints ─────────────────────────────────────────────

    @PostMapping("/recruiters/{recruiterId}/team")
    @Operation(summary = "Invite a user to the recruiter's team")
    public ResponseEntity<TeamMember> inviteTeamMember(
            @PathVariable int recruiterId,
            @Valid @RequestBody TeamMember member) {
        log.info("POST /profiles/recruiters/{}/team email={}", recruiterId, member.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.inviteTeamMember(recruiterId, member));
    }

    @GetMapping("/recruiters/{recruiterId}/team")
    @Operation(summary = "Get all team members for a recruiter")
    public ResponseEntity<List<TeamMember>> getTeamMembers(@PathVariable int recruiterId) {
        return ResponseEntity.ok(profileService.getTeamMembers(recruiterId));
    }

    @GetMapping("/recruiters/{recruiterId}/team/status/{status}")
    @Operation(summary = "Get team members by status (PENDING | ACCEPTED | REVOKED)")
    public ResponseEntity<List<TeamMember>> getTeamMembersByStatus(
            @PathVariable int recruiterId,
            @PathVariable String status) {
        return ResponseEntity.ok(profileService.getTeamMembersByStatus(recruiterId, status));
    }

    @PatchMapping("/recruiters/{recruiterId}/team/{memberUserId}/accept")
    @Operation(summary = "Accept a team invitation (called by the invited member)")
    public ResponseEntity<TeamMember> acceptInvitation(
            @PathVariable int recruiterId,
            @PathVariable int memberUserId) {
        return ResponseEntity.ok(profileService.acceptInvitation(recruiterId, memberUserId));
    }

    @PatchMapping("/recruiters/{recruiterId}/team/{memberUserId}/role")
    @Operation(summary = "Update a team member's role (ADMIN | MANAGER | VIEWER)")
    public ResponseEntity<TeamMember> updateRole(
            @PathVariable int recruiterId,
            @PathVariable int memberUserId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                profileService.updateTeamMemberRole(recruiterId, memberUserId, body.get("role")));
    }

    @DeleteMapping("/recruiters/{recruiterId}/team/{teamMemberId}")
    @Operation(summary = "Revoke a team member's access")
    public ResponseEntity<Map<String, String>> revokeTeamMember(
            @PathVariable int recruiterId,
            @PathVariable int teamMemberId) {
        profileService.revokeTeamMember(recruiterId, teamMemberId);
        return ResponseEntity.ok(Map.of("message", "Team member revoked successfully"));
    }

    @GetMapping("/recruiters/{recruiterId}/team/{memberUserId}/check")
    @Operation(summary = "Check if a user is a team member of this recruiter")
    public ResponseEntity<Map<String, Boolean>> checkTeamMember(
            @PathVariable int recruiterId,
            @PathVariable int memberUserId) {
        return ResponseEntity.ok(
                Map.of("isMember", profileService.isTeamMember(recruiterId, memberUserId)));
    }
}
