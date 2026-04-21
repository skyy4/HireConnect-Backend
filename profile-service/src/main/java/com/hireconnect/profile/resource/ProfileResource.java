package com.hireconnect.profile.resource;

import com.hireconnect.profile.pojo.CandidateProfile;
import com.hireconnect.profile.pojo.RecruiterProfile;
import com.hireconnect.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Service", description = "Candidate and Recruiter profiles")
public class ProfileResource {

    private final ProfileService profileService;

    // ── Candidate Endpoints ────────────────────────────────────────────────
    @PostMapping("/candidates")
    @Operation(summary = "Create candidate profile")
    public ResponseEntity<CandidateProfile> createCandidate(@Valid @RequestBody CandidateProfile profile) {
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
        profileService.deleteCandidateProfile(profileId);
        return ResponseEntity.ok(Map.of("message", "Candidate profile deleted"));
    }

    // ── Recruiter Endpoints ────────────────────────────────────────────────
    @PostMapping("/recruiters")
    @Operation(summary = "Create recruiter profile")
    public ResponseEntity<RecruiterProfile> createRecruiter(@Valid @RequestBody RecruiterProfile profile) {
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
}
