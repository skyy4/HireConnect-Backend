package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
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
        List<Map<String, Object>> jobs = safeGetJobList(jobServiceUrl + "/api/v1/jobs/recruiter/" + recruiterId);
        long totalJobs = jobs.size();

        long totalApps = 0L;
        long shortlisted = 0L;
        long offered = 0L;
        long rejected = 0L;
        long totalViews = 0L;

        for (Map<String, Object> job : jobs) {
            int jobId = toInt(job.get("jobId"));
            if (jobId <= 0) {
                continue;
            }
            totalApps += safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count");
            shortlisted += safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count?status=SHORTLISTED");
            offered += safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count?status=OFFERED");
            rejected += safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count?status=REJECTED");
            totalViews += safeGetLong(jobServiceUrl + "/api/v1/jobs/" + jobId + "/views/count");
        }

        double avgTimeToHireDays = calculateAverageTimeToHire(jobs);

        return AnalyticsSummary.builder()
                .recruiterId(recruiterId)
                .totalJobs(totalJobs)
                .totalJobsPosted(totalJobs)
                .totalApplications(totalApps)
                .totalApplicationsReceived(totalApps)
                .shortlistedCount(shortlisted)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .avgTimeToHireDays(avgTimeToHireDays)
                .viewToApplyRatio(totalViews > 0 ? (double) totalApps / totalViews : 0)
                .build();
    }

    @Override
    public AnalyticsSummary getPlatformStats() {
        long activeJobs = safeGetLong(jobServiceUrl + "/api/v1/jobs/count?status=ACTIVE");
        long totalApplications = safeGetLong(applicationServiceUrl + "/api/v1/applications/count");
        long activeSubscriptions = safeGetSubscriptionList(subscriptionServiceUrl + "/api/v1/subscriptions/admin").stream()
                .filter(sub -> "ACTIVE".equalsIgnoreCase(String.valueOf(sub.get("status"))))
                .count();

        return AnalyticsSummary.builder()
                .totalActiveJobs(activeJobs)
                .totalJobsAllTime(activeJobs)
                .totalApplications(totalApplications)
                .totalApplicationsAllTime(totalApplications)
                .activeSubscriptions(activeSubscriptions)
                .build();
    }

    @Override
    public AnalyticsSummary getJobAnalytics(int jobId) {
        long appCount = safeGetLong(applicationServiceUrl + "/api/v1/applications/job/" + jobId + "/count");
        long viewCount = safeGetLong(jobServiceUrl + "/api/v1/jobs/" + jobId + "/views/count");
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
        return safeGetLong(jobServiceUrl + "/api/v1/jobs/" + jobId + "/views/count");
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
        List<Map<String, Object>> jobs = safeGetJobList(jobServiceUrl + "/api/v1/jobs/recruiter/" + recruiterId);
        return calculateAverageTimeToHire(jobs);
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private long safeGetLong(String url) {
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            if (response instanceof Number n) {
                return n.longValue();
            }
            if (response instanceof Map<?, ?> map) {
                for (String key : List.of("count", "viewCount", "totalJobs", "totalApplications")) {
                    Object value = map.get(key);
                    if (value instanceof Number n) {
                        return n.longValue();
                    }
                }
            }
            if (response instanceof List<?> list) {
                return list.size();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from {}: {}", url, e.getMessage());
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeGetJobList(String url) {
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            if (response instanceof List<?> list) {
                return (List<Map<String, Object>>) list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch jobs from {}: {}", url, e.getMessage());
        }
        return List.of();
    }

    private int toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return -1;
    }

    private double calculateAverageTimeToHire(List<Map<String, Object>> jobs) {
        double totalDays = 0.0;
        long count = 0L;

        for (Map<String, Object> job : jobs) {
            int jobId = toInt(job.get("jobId"));
            if (jobId <= 0) {
                continue;
            }

            List<Map<String, Object>> offeredApplications = safeGetApplicationList(
                    applicationServiceUrl + "/api/v1/applications/job/" + jobId + "?status=OFFERED");

            for (Map<String, Object> app : offeredApplications) {
                LocalDateTime appliedAt = toDateTime(app.get("appliedAt"));
                LocalDateTime statusUpdatedAt = toDateTime(app.get("statusUpdatedAt"));
                if (appliedAt == null) {
                    continue;
                }

                LocalDateTime end = statusUpdatedAt != null ? statusUpdatedAt : appliedAt;
                if (end.isBefore(appliedAt)) {
                    continue;
                }

                totalDays += Duration.between(appliedAt, end).toMinutes() / 1440.0;
                count++;
            }
        }

        return count > 0 ? totalDays / count : 0.0;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeGetApplicationList(String url) {
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            if (response instanceof List<?> list) {
                return (List<Map<String, Object>>) list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch applications from {}: {}", url, e.getMessage());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeGetSubscriptionList(String url) {
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            if (response instanceof List<?> list) {
                return (List<Map<String, Object>>) list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch subscriptions from {}: {}", url, e.getMessage());
        }
        return List.of();
    }

    private LocalDateTime toDateTime(Object value) {
        if (value instanceof LocalDateTime time) {
            return time;
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return LocalDateTime.parse(str);
            } catch (Exception ignored) {
                log.debug("Unable to parse timestamp {}", str);
            }
        }
        return null;
    }
}
