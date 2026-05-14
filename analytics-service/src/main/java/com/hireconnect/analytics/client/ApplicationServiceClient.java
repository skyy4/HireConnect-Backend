package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign Client for synchronous communication with application-service.
 */
@FeignClient(name = "application-service", url = "${services.application-service:http://application-service:8084}")
public interface ApplicationServiceClient {

    @GetMapping("/api/v1/applications/job/{jobId}/count")
    Map<String, Long> getApplicationCount(@PathVariable("jobId") int jobId,
                                          @RequestParam(required = false) String status);

    @GetMapping("/api/v1/applications/count")
    Map<String, Long> getTotalApplicationCount();
}
