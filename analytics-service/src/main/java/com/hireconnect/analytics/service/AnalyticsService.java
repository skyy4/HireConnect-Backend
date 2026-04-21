package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;

public interface AnalyticsService {

    AnalyticsSummary getRecruiterAnalytics(int recruiterId);

    AnalyticsSummary getPlatformStats();

    AnalyticsSummary getJobAnalytics(int jobId);

    long getJobViewCount(int jobId);

    long getAppCountByJob(int jobId);

    double getViewToApplyRatio(int jobId);

    double getTimeToHire(int recruiterId);
}
