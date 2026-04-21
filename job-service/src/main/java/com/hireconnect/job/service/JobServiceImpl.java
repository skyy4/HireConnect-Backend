package com.hireconnect.job.service;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${app.services.subscription-url}")
    private String subscriptionUrl;

    @Override
    @Transactional
    public Job addJob(Job job) {
        // Enforce Freemium Limits
        checkPostingLimit(job.getPostedBy());

        job.setPostedAt(LocalDateTime.now());
        job.setStatus("ACTIVE");
        job.setViewCount(0);
        return jobRepository.save(job);
    }

    private void checkPostingLimit(int recruiterId) {
        try {
            // Fetch subscription from subscription-service
            String url = subscriptionUrl + "/api/v1/subscriptions/recruiter/" + recruiterId + "/active";
            java.util.Map<String, Object> sub = restTemplate.getForObject(url, java.util.Map.class);
            
            if (sub != null && "FREE".equalsIgnoreCase((String) sub.get("plan"))) {
                long activeJobs = jobRepository.countByPostedByAndStatus(recruiterId, "ACTIVE");
                if (activeJobs >= 3) {
                    throw new IllegalStateException("Free plan limit reached (3 active jobs). Upgrade to Professional to post more.");
                }
            }
        } catch (Exception e) {
            // Fallback: If subscription service is down, still enforce a safe limit for FREE users
            // In production, we might want a different strategy
            long activeJobs = jobRepository.countByPostedByAndStatus(recruiterId, "ACTIVE");
            if (activeJobs >= 3) {
                throw new IllegalStateException("Maximum job limit reached for your current plan.");
            }
        }
    }

    @Override
    public Job getJobById(int jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findByStatus("ACTIVE");
    }

    @Override
    public List<Job> getJobsByRecruiter(int recruiterId) {
        return jobRepository.findByPostedBy(recruiterId);
    }

    @Override
    public List<Job> searchJobs(String title, String location, String category,
                                String type, String experienceLevel,
                                Double minSalary, Double maxSalary) {
        return jobRepository.searchJobs(title, location, category, type, experienceLevel, minSalary, maxSalary);
    }

    @Override
    @Transactional
    public Job updateJob(int jobId, Job update) {
        Job existing = getJobById(jobId);
        update.setJobId(existing.getJobId());
        update.setPostedBy(existing.getPostedBy());
        update.setPostedAt(existing.getPostedAt());
        update.setViewCount(existing.getViewCount());
        update.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(update);
    }

    @Override
    @Transactional
    public void deleteJob(int jobId) {
        jobRepository.deleteById(jobId);
    }

    @Override
    @Transactional
    public Job updateJobStatus(int jobId, String status) {
        Job job = getJobById(jobId);
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job incrementViewCount(int jobId) {
        Job job = getJobById(jobId);
        job.setViewCount(job.getViewCount() + 1);
        return jobRepository.save(job);
    }

    @Override
    public List<Job> getJobsByStatus(String status) {
        return jobRepository.findByStatus(status);
    }

    @Override
    public long countJobsByRecruiter(int recruiterId) {
        return jobRepository.countByPostedBy(recruiterId);
    }
}
