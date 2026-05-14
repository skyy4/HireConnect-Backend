package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for synchronous communication with job-service.
 */
@FeignClient(name = "job-service", url = "${services.job-service:http://job-service:8083}")
public interface JobServiceClient {

    @GetMapping("/api/v1/jobs/recruiter/{recruiterId}")
    List<Map<String, Object>> getJobsByRecruiter(@PathVariable("recruiterId") int recruiterId);

    @GetMapping("/api/v1/jobs/count")
    Map<String, Long> getJobCount(@RequestParam(required = false) String status);

    @GetMapping("/api/v1/jobs/{jobId}/views/count")
    Map<String, Long> getViewCount(@PathVariable("jobId") int jobId);
}
