package com.hireconnect.analytics.resource;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import com.hireconnect.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Service", description = "Hiring metrics and platform reporting")
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GetMapping("/recruiter/{recruiterId}")
    @Operation(summary = "Get analytics dashboard for a recruiter")
    public ResponseEntity<AnalyticsSummary> getRecruiterAnalytics(@PathVariable int recruiterId) {
        log.info("GET /analytics/recruiter/{}", recruiterId);
        return ResponseEntity.ok(analyticsService.getRecruiterAnalytics(recruiterId));
    }

    @GetMapping("/admin")
    @Operation(summary = "Get platform-wide analytics (Admin only)")
    public ResponseEntity<AnalyticsSummary> getPlatformStats() {
        log.info("GET /analytics/admin — platform-wide stats requested");
        return ResponseEntity.ok(analyticsService.getPlatformStats());
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get per-job analytics (view count, apply rate)")
    public ResponseEntity<AnalyticsSummary> getJobAnalytics(@PathVariable int jobId) {
        log.debug("GET /analytics/job/{}", jobId);
        return ResponseEntity.ok(analyticsService.getJobAnalytics(jobId));
    }
}
