package com.hireconnect.application.resource;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.service.ApplicationService;
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
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Application Service", description = "Job application submission and tracking")
public class ApplicationResource {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Submit a job application (Candidate)")
    public ResponseEntity<Application> submit(@Valid @RequestBody Application application) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.submitApplication(application));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by ID")
    public ResponseEntity<Application> getById(@PathVariable int applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get all applications for a candidate")
    public ResponseEntity<List<Application>> getByCandidate(@PathVariable int candidateId) {
        return ResponseEntity.ok(applicationService.getByCandidate(candidateId));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get all applications for a job (Recruiter)")
    public ResponseEntity<List<Application>> getByJob(
            @PathVariable int jobId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(applicationService.getByJobAndStatus(jobId, status));
        }
        return ResponseEntity.ok(applicationService.getByJob(jobId));
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update application status (Recruiter)")
    public ResponseEntity<Application> updateStatus(
            @PathVariable int applicationId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                applicationService.updateStatus(applicationId, body.get("status"), body.get("note")));
    }

    @PatchMapping("/{applicationId}/withdraw")
    @Operation(summary = "Withdraw an application (Candidate)")
    public ResponseEntity<Map<String, String>> withdraw(
            @PathVariable int applicationId,
            @RequestParam int candidateId) {
        applicationService.withdrawApplication(applicationId, candidateId);
        return ResponseEntity.ok(Map.of("message", "Application withdrawn successfully"));
    }

    @GetMapping("/job/{jobId}/count")
    @Operation(summary = "Count applications for a job")
    public ResponseEntity<Map<String, Long>> countByJob(@PathVariable int jobId) {
        return ResponseEntity.ok(Map.of("count", applicationService.countByJob(jobId)));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if candidate has already applied to a job")
    public ResponseEntity<Map<String, Boolean>> hasApplied(
            @RequestParam int jobId,
            @RequestParam int candidateId) {
        return ResponseEntity.ok(Map.of("applied", applicationService.hasApplied(jobId, candidateId)));
    }
}
