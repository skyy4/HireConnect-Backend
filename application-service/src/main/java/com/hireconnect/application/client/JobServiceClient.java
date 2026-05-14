package com.hireconnect.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for synchronous communication with job-service.
 * Used to validate job existence before accepting an application.
 */
@FeignClient(name = "job-service", url = "${services.job-service:http://job-service:8083}")
public interface JobServiceClient {

    @GetMapping("/api/v1/jobs/{jobId}")
    Map<String, Object> getJobById(@PathVariable("jobId") int jobId);

    @GetMapping("/api/v1/jobs/{jobId}/active")
    Boolean isJobActive(@PathVariable("jobId") int jobId);
}
