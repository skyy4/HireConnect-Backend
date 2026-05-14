package com.hireconnect.job.resource;

import com.hireconnect.job.entity.Bookmark;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobView;
import com.hireconnect.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Service", description = "Job posting management, search, bookmarks and view tracking")
public class JobResource {

    private final JobService jobService;

    // ── Job CRUD ──────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Post a new job opening (Recruiter)")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job) {
        log.info("POST /jobs title='{}' recruiterId={}", job.getTitle(), job.getPostedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJob(job));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details by ID and record the view")
    public ResponseEntity<Job> getJob(
            @PathVariable int jobId,
            @RequestParam(required = false) Integer viewerId,
            @RequestParam(required = false) String source,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        // Record per-user view for analytics (replaces the simple incrementViewCount)
        jobService.recordView(jobId, viewerId, ip, source);
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping
    @Operation(summary = "Get all active jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter jobs by title, location, category, type, level, salary")
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
    @Operation(summary = "Update job status: ACTIVE | PAUSED | CLOSED")
    public ResponseEntity<Job> updateStatus(
            @PathVariable int jobId,
            @RequestBody Map<String, String> body) {
        log.info("PATCH /jobs/{}/status newStatus={}", jobId, body.get("status"));
        return ResponseEntity.ok(jobService.updateJobStatus(jobId, body.get("status")));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete a job posting (Recruiter)")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable int jobId) {
        log.warn("DELETE /jobs/{}", jobId);
        jobService.deleteJob(jobId);
        log.info("Job deleted: jobId={}", jobId);
        return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
    }

    @GetMapping("/recruiter/{recruiterId}/count")
    @Operation(summary = "Count total jobs posted by recruiter")
    public ResponseEntity<Map<String, Long>> countByRecruiter(@PathVariable int recruiterId) {
        long count = jobService.countJobsByRecruiter(recruiterId);
        return ResponseEntity.ok(Map.of(
                "count", count,
                "totalJobs", count));
    }

    @GetMapping("/count")
    @Operation(summary = "Count jobs (optionally filtered by status)")
    public ResponseEntity<Map<String, Long>> countJobs(@RequestParam(required = false) String status) {
        long count = (status == null || status.isBlank())
                ? jobService.countAllJobs()
                : jobService.countJobsByStatus(status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ── Bookmarks (Saved Jobs) ─────────────────────────────────────────────

    @PostMapping("/bookmarks")
    @Operation(summary = "Bookmark (save) a job for a candidate")
    public ResponseEntity<Bookmark> addBookmark(@RequestBody Map<String, Object> body) {
        int candidateId = (Integer) body.get("candidateId");
        int jobId       = (Integer) body.get("jobId");
        String note     = (String) body.getOrDefault("note", null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.addBookmark(candidateId, jobId, note));
    }

    @DeleteMapping("/bookmarks/{candidateId}/{jobId}")
    @Operation(summary = "Remove a bookmark (un-save) for a candidate")
    public ResponseEntity<Map<String, String>> removeBookmark(
            @PathVariable int candidateId,
            @PathVariable int jobId) {
        jobService.removeBookmark(candidateId, jobId);
        return ResponseEntity.ok(Map.of("message", "Bookmark removed successfully"));
    }

    @GetMapping("/bookmarks/candidate/{candidateId}")
    @Operation(summary = "Get all bookmarked jobs for a candidate")
    public ResponseEntity<List<Bookmark>> getBookmarks(@PathVariable int candidateId) {
        return ResponseEntity.ok(jobService.getBookmarksByCandidate(candidateId));
    }

    @GetMapping("/bookmarks/check")
    @Operation(summary = "Check if a job is bookmarked by a candidate")
    public ResponseEntity<Map<String, Boolean>> isBookmarked(
            @RequestParam int candidateId,
            @RequestParam int jobId) {
        return ResponseEntity.ok(Map.of("bookmarked", jobService.isBookmarked(candidateId, jobId)));
    }

    @GetMapping("/{jobId}/bookmarks/count")
    @Operation(summary = "Count total bookmarks for a job")
    public ResponseEntity<Map<String, Long>> countBookmarks(@PathVariable int jobId) {
        return ResponseEntity.ok(Map.of("bookmarkCount", jobService.countBookmarksByJob(jobId)));
    }

    // ── Job View Tracking ─────────────────────────────────────────────────

    @GetMapping("/{jobId}/views")
    @Operation(summary = "Get all view events for a job (Analytics use)")
    public ResponseEntity<List<JobView>> getViews(@PathVariable int jobId) {
        return ResponseEntity.ok(jobService.getViewsByJob(jobId));
    }

    @GetMapping("/{jobId}/views/count")
    @Operation(summary = "Get total view count for a job")
    public ResponseEntity<Map<String, Long>> getViewCount(@PathVariable int jobId) {
        long viewCount = jobService.getViewCount(jobId);
        return ResponseEntity.ok(Map.of(
                "count", viewCount,
                "viewCount", viewCount));
    }

    @GetMapping("/views/top")
    @Operation(summary = "Get top viewed jobs (Analytics use)")
    public ResponseEntity<List<Object[]>> getTopViewed() {
        return ResponseEntity.ok(jobService.getTopViewedJobs());
    }
}
