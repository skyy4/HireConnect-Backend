package com.hireconnect.analytics.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {

    // Recruiter-level metrics
    private int recruiterId;
    private long totalJobs;
    private long totalJobsPosted;
    private long totalApplications;
    private long totalApplicationsReceived;
    private long shortlistedCount;
    private long offeredCount;
    private long rejectedCount;
    private double avgTimeToHireDays;
    private double viewToApplyRatio;

    // Platform-level metrics (Admin)
    private long totalActiveJobs;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalJobsAllTime;
    private long totalApplicationsAllTime;
    private long totalInterviewsScheduled;
    private long activeSubscriptions;

    // Per-job metrics
    private int jobId;
    private int viewCount;
    private long applicationCount;
    private double jobViewToApplyRatio;
}
