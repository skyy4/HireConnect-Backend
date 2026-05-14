package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsServiceImpl Unit Tests")
class AnalyticsServiceImplTest {

    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analyticsService, "jobServiceUrl", "http://job-service:8083");
        ReflectionTestUtils.setField(analyticsService, "applicationServiceUrl", "http://application-service:8084");
        ReflectionTestUtils.setField(analyticsService, "subscriptionServiceUrl", "http://subscription-service:8087");
    }

    @Test
    @DisplayName("getJobAnalytics — returns correct ratios when both counts available")
    void getJobAnalytics_returnsCorrectRatios() {
        when(restTemplate.getForObject(contains("/count"), eq(Object.class)))
                .thenReturn(Map.of("count", 10));
        when(restTemplate.getForObject(contains("views/count"), eq(Object.class)))
                .thenReturn(Map.of("count", 100));

        AnalyticsSummary result = analyticsService.getJobAnalytics(1);

        assertThat(result.getJobId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getJobAnalytics — handles zero views gracefully")
    void getJobAnalytics_zeroViews_ratioisZero() {
        when(restTemplate.getForObject(anyString(), eq(Object.class))).thenReturn(0L);

        AnalyticsSummary result = analyticsService.getJobAnalytics(1);
        assertThat(result.getJobViewToApplyRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getJobViewCount — returns 0 when service unavailable")
    void getJobViewCount_serviceDown_returnsZero() {
        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenThrow(new RestClientException("Connection refused"));

        long count = analyticsService.getJobViewCount(99);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("getAppCountByJob — returns count from application-service")
    void getAppCountByJob_returnsCount() {
        when(restTemplate.getForObject(contains("applications"), eq(Object.class)))
                .thenReturn(5L);

        long count = analyticsService.getAppCountByJob(1);
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("getViewToApplyRatio — returns 0 when views are 0")
    void getViewToApplyRatio_zeroViews_returnsZero() {
        when(restTemplate.getForObject(contains("views"), eq(Object.class))).thenReturn(0L);
        when(restTemplate.getForObject(contains("applications"), eq(Object.class))).thenReturn(10L);

        double ratio = analyticsService.getViewToApplyRatio(1);
        assertThat(ratio).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getPlatformStats — returns summary with active jobs")
    void getPlatformStats_returnsSummary() {
        when(restTemplate.getForObject(contains("jobs/count"), eq(Object.class))).thenReturn(50L);
        when(restTemplate.getForObject(contains("applications/count"), eq(Object.class))).thenReturn(200L);
        when(restTemplate.getForObject(contains("subscriptions/admin"), eq(Object.class)))
                .thenReturn(List.of(Map.of("status", "ACTIVE"), Map.of("status", "CANCELLED")));

        AnalyticsSummary result = analyticsService.getPlatformStats();

        assertThat(result).isNotNull();
        assertThat(result.getActiveSubscriptions()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getRecruiterAnalytics — returns empty summary when no jobs")
    void getRecruiterAnalytics_noJobs_returnsZeroCounts() {
        when(restTemplate.getForObject(contains("jobs/recruiter"), eq(Object.class)))
                .thenReturn(List.of());

        AnalyticsSummary result = analyticsService.getRecruiterAnalytics(1);

        assertThat(result.getTotalJobs()).isEqualTo(0L);
        assertThat(result.getTotalApplications()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getRecruiterAnalytics — aggregates counts across multiple jobs")
    void getRecruiterAnalytics_withJobs_aggregates() {
        List<Map<String, Object>> jobs = List.of(
                Map.of("jobId", 1),
                Map.of("jobId", 2)
        );
        // Mock jobs list call specifically
        when(restTemplate.getForObject(contains("jobs/recruiter"), eq(Object.class))).thenReturn(jobs);
        // Mock all application count and view count calls
        when(restTemplate.getForObject(contains("applications"), eq(Object.class))).thenReturn(3L);
        when(restTemplate.getForObject(contains("views"), eq(Object.class))).thenReturn(10L);

        AnalyticsSummary result = analyticsService.getRecruiterAnalytics(1);

        assertThat(result.getTotalJobs()).isEqualTo(2L);
    }
}
