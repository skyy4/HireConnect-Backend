package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${services.job-service:http://localhost:8083}")
    private String jobServiceUrl;

    @Value("${services.application-service:http://localhost:8084}")
    private String applicationServiceUrl;

    @Value("${services.subscription-service:http://localhost:8087}")
    private String subscriptionServiceUrl;

    @Override
    public AnalyticsSummary getRecruiterAnalytics(int recruiterId) {
        long totalJobs = safeGetLong(jobServiceUrl + "/api/v1/jobs/recruiter/" + recruiterId + "/count");
        long totalApps = safeGetAppsForRecruiter(recruiterId);
        long shortlisted = safeGetLong(applicationServiceUrl + "/api/v1/applications/recruiter/" + recruiterId + "/count-by-status?status=SHORTLISTED");
        long offered = safeGetLong(applicationServiceUrl + "/api/v1/applications/recruiter/" + recruiterId + "/count-by-status?status=OFFERED");
        long rejected = safeGetLong(applicationServiceUrl + "/api/v1/applications/recruiter/" + recruiterId + "/count-by-status?status=REJECTED");

        return AnalyticsSummary.builder()
                .recruiterId(recruiterId)
                .totalJobsPosted(totalJobs)
                .totalApplicationsReceived(totalApps)
                .shortlistedCount(shortlisted)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .avgTimeToHireDays(getTimeToHire(recruiterId))
                .viewToApplyRatio(totalApps > 0 ? (double) totalApps / Math.max(totalJobs, 1) : 0)
                .build();
    }

    @Override
    public AnalyticsSummary getPlatformStats() {
        return AnalyticsSummary.builder()
                .totalActiveJobs(safeGetLong(jobServiceUrl + "/api/v1/jobs/count?status=ACTIVE"))
                .totalApplicationsAllTime(safeGetLong(applicationServiceUrl + "/api/v1/applications/count"))
                .build();
    }

    @Override
    public AnalyticsSummary getJobAnalytics(int jobId) {
        long appCount = safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count");
        long viewCount = safeGetLong(jobServiceUrl + "/api/v1/jobs/" + jobId + "/views");
        double ratio = viewCount > 0 ? (double) appCount / viewCount : 0;

        return AnalyticsSummary.builder()
                .jobId(jobId)
                .viewCount((int) viewCount)
                .applicationCount(appCount)
                .jobViewToApplyRatio(ratio)
                .build();
    }

    @Override
    public long getJobViewCount(int jobId) {
        return safeGetLong(jobServiceUrl + "/api/v1/jobs/" + jobId + "/views");
    }

    @Override
    public long getAppCountByJob(int jobId) {
        return safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count");
    }

    @Override
    public double getViewToApplyRatio(int jobId) {
        long views = getJobViewCount(jobId);
        long apps = getAppCountByJob(jobId);
        return views > 0 ? (double) apps / views : 0;
    }

    @Override
    public double getTimeToHire(int recruiterId) {
        // Simplified: would normally compute from application APPLIED→OFFERED date diff
        return 14.5; // placeholder avg days
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private long safeGetLong(String url) {
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                Object count = response.get("count");
                if (count instanceof Number n) return n.longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from {}: {}", url, e.getMessage());
        }
        return 0L;
    }

    private long safeGetAppsForRecruiter(int recruiterId) {
        // Would query application-service for all jobs by recruiter then sum
        return safeGetLong(applicationServiceUrl + "/api/v1/applications/count?recruiterId=" + recruiterId);
    }
}
