package com.hireconnect.job.resource;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.service.JobService;
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
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Service", description = "Job posting management and search")
public class JobResource {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Post a new job opening (Recruiter)")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJob(job));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details by ID")
    public ResponseEntity<Job> getJob(@PathVariable int jobId) {
        jobService.incrementViewCount(jobId); // track view
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping
    @Operation(summary = "Get all active jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter jobs")
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary) {
        return ResponseEntity.ok(
                jobService.searchJobs(title, location, category, type, experienceLevel, minSalary, maxSalary));
    }

    @GetMapping("/recruiter/{recruiterId}")
    @Operation(summary = "Get all jobs posted by a recruiter")
    public ResponseEntity<List<Job>> getJobsByRecruiter(@PathVariable int recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    @PutMapping("/{jobId}")
    @Operation(summary = "Update job posting (Recruiter)")
    public ResponseEntity<Job> updateJob(@PathVariable int jobId, @Valid @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(jobId, job));
    }

    @PatchMapping("/{jobId}/status")
    @Operation(summary = "Update job status (ACTIVE | PAUSED | CLOSED)")
    public ResponseEntity<Job> updateStatus(
            @PathVariable int jobId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(jobService.updateJobStatus(jobId, body.get("status")));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete a job posting (Recruiter)")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable int jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
    }

    @GetMapping("/recruiter/{recruiterId}/count")
    @Operation(summary = "Count total jobs posted by recruiter")
    public ResponseEntity<Map<String, Long>> countByRecruiter(@PathVariable int recruiterId) {
        return ResponseEntity.ok(Map.of("count", jobService.countJobsByRecruiter(recruiterId)));
    }
}
