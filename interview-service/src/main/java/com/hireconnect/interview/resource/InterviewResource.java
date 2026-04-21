package com.hireconnect.interview.resource;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Service", description = "Interview scheduling and management")
public class InterviewResource {

    private final InterviewService interviewService;

    @PostMapping
    @Operation(summary = "Schedule an interview (Recruiter)")
    public ResponseEntity<Interview> schedule(@RequestBody Interview interview) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.scheduleInterview(interview));
    }

    @GetMapping("/{interviewId}")
    @Operation(summary = "Get interview by ID")
    public ResponseEntity<Interview> getById(@PathVariable int interviewId) {
        return ResponseEntity.ok(interviewService.getInterviewById(interviewId));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get interviews by application")
    public ResponseEntity<List<Interview>> getByApplication(@PathVariable int applicationId) {
        return ResponseEntity.ok(interviewService.getByApplicationId(applicationId));
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get all interviews for a candidate")
    public ResponseEntity<List<Interview>> getByCandidate(@PathVariable int candidateId) {
        return ResponseEntity.ok(interviewService.getByCandidate(candidateId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    @Operation(summary = "Get all interviews managed by recruiter")
    public ResponseEntity<List<Interview>> getByRecruiter(@PathVariable int recruiterId) {
        return ResponseEntity.ok(interviewService.getByRecruiter(recruiterId));
    }

    @PatchMapping("/{interviewId}/confirm")
    @Operation(summary = "Confirm an interview (Candidate)")
    public ResponseEntity<Interview> confirm(@PathVariable int interviewId) {
        return ResponseEntity.ok(interviewService.confirmInterview(interviewId));
    }

    @PatchMapping("/{interviewId}/reschedule")
    @Operation(summary = "Reschedule an interview")
    public ResponseEntity<Interview> reschedule(
            @PathVariable int interviewId,
            @RequestBody Map<String, String> body) {
        LocalDateTime newTime = LocalDateTime.parse(body.get("scheduledAt"));
        return ResponseEntity.ok(interviewService.rescheduleInterview(interviewId, newTime, body.get("notes")));
    }

    @PatchMapping("/{interviewId}/cancel")
    @Operation(summary = "Cancel an interview")
    public ResponseEntity<Interview> cancel(
            @PathVariable int interviewId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(interviewService.cancelInterview(interviewId, body.get("reason")));
    }

    @GetMapping("/range")
    @Operation(summary = "Get interviews scheduled within a date range")
    public ResponseEntity<List<Interview>> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(interviewService.getScheduledBetween(from, to));
    }
}
