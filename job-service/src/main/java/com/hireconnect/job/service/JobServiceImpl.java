package com.hireconnect.job.service;

import com.hireconnect.job.entity.Bookmark;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobView;
import com.hireconnect.job.repository.BookmarkRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobViewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
    private static final String NOTIFICATION_EXCHANGE = "hireconnect.notifications";

    private final JobRepository jobRepository;
    private final BookmarkRepository bookmarkRepository;
    private final JobViewRepository jobViewRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.services.subscription-url}")
    private String subscriptionUrl;

    // ── Job CRUD ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = {"jobs", "searchJobs"}, allEntries = true)
    public Job addJob(Job job) {
        checkPostingLimit(job.getPostedBy());
        job.setPostedAt(LocalDateTime.now());
        job.setStatus("ACTIVE");
        job.setViewCount(0);
        Job saved = jobRepository.save(job);
        publishNewJobEvent(saved);
        return saved;
    }

    private void publishNewJobEvent(Job job) {
        try {
            // Format: JOB_POSTED:<jobId>:<title>:<category>:<location>
            String message = String.format("JOB_POSTED:%d:%s:%s:%s",
                    job.getJobId(),
                    job.getTitle() != null ? job.getTitle() : "",
                    job.getCategory() != null ? job.getCategory() : "",
                    job.getLocation() != null ? job.getLocation() : "");
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "", message);
            log.info("Published new job event: {}", message);
        } catch (Exception ex) {
            log.warn("Failed to publish new job event: {}", ex.getMessage());
        }
    }

    private void checkPostingLimit(int recruiterId) {
        try {
            String url = subscriptionUrl + "/api/v1/subscriptions/recruiter/" + recruiterId + "/active";
            @SuppressWarnings("unchecked")
            Map<String, Object> sub = restTemplate.getForObject(url, Map.class);
            if (sub != null && "FREE".equalsIgnoreCase((String) sub.get("plan"))) {
                long activeJobs = jobRepository.countByPostedByAndStatus(recruiterId, "ACTIVE");
                if (activeJobs >= 3) {
                    throw new IllegalStateException(
                            "Free plan limit reached (3 active jobs). Upgrade to Professional to post more.");
                }
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception e) {
            long activeJobs = jobRepository.countByPostedByAndStatus(recruiterId, "ACTIVE");
            if (activeJobs >= 3) {
                throw new IllegalStateException("Maximum job limit reached for your current plan.");
            }
        }
    }

    @Override
    @Cacheable(value = "jobs", key = "#jobId")
    public Job getJobById(int jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));
    }

    @Override
    @Cacheable(value = "jobs", key = "'allActive'")
    public List<Job> getAllJobs() {
        return jobRepository.findByStatus("ACTIVE");
    }

    @Override
    public List<Job> getJobsByRecruiter(int recruiterId) {
        return jobRepository.findByPostedBy(recruiterId);
    }

    @Override
    @Cacheable(value = "searchJobs")
    public List<Job> searchJobs(String title, String location, String category,
                                String type, String experienceLevel,
                                Double minSalary, Double maxSalary) {
        return jobRepository.searchJobs(title, location, category, type, experienceLevel, minSalary, maxSalary);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#jobId"),
        @CacheEvict(value = "jobs", key = "'allActive'"),
        @CacheEvict(value = "searchJobs", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#jobId"),
        @CacheEvict(value = "jobs", key = "'allActive'"),
        @CacheEvict(value = "searchJobs", allEntries = true)
    })
    public void deleteJob(int jobId) {
        jobRepository.deleteById(jobId);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#jobId"),
        @CacheEvict(value = "jobs", key = "'allActive'"),
        @CacheEvict(value = "searchJobs", allEntries = true)
    })
    public Job updateJobStatus(int jobId, String status) {
        Job job = getJobById(jobId);
        String currentStatus = job.getStatus();
        
        if (!"ACTIVE".equals(status) && !"PAUSED".equals(status) && !"CLOSED".equals(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: ACTIVE, PAUSED, CLOSED");
        }
        
        if ("CLOSED".equals(currentStatus) && !"CLOSED".equals(status)) {
            throw new IllegalStateException("Cannot change status of a CLOSED job.");
        }
        
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    @CacheEvict(value = "jobs", key = "#jobId")
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

    @Override
    public long countJobsByStatus(String status) {
        return jobRepository.countByStatus(status);
    }

    @Override
    public long countAllJobs() {
        return jobRepository.count();
    }

    // ── Bookmark (Saved Jobs) ─────────────────────────────────────────────

    @Override
    @Transactional
    public Bookmark addBookmark(int candidateId, int jobId, String note) {
        if (bookmarkRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new IllegalStateException(
                    "Job " + jobId + " is already bookmarked by candidate " + candidateId);
        }
        // Verify job exists before bookmarking
        getJobById(jobId);
        Bookmark bookmark = Bookmark.builder()
                .candidateId(candidateId)
                .jobId(jobId)
                .note(note)
                .savedAt(LocalDateTime.now())
                .build();
        return bookmarkRepository.save(bookmark);
    }

    @Override
    @Transactional
    public void removeBookmark(int candidateId, int jobId) {
        if (!bookmarkRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new IllegalArgumentException(
                    "Bookmark not found for candidate " + candidateId + " and job " + jobId);
        }
        bookmarkRepository.deleteByCandidateIdAndJobId(candidateId, jobId);
    }

    @Override
    public List<Bookmark> getBookmarksByCandidate(int candidateId) {
        return bookmarkRepository.findByCandidateId(candidateId);
    }

    @Override
    public boolean isBookmarked(int candidateId, int jobId) {
        return bookmarkRepository.existsByCandidateIdAndJobId(candidateId, jobId);
    }

    @Override
    public long countBookmarksByJob(int jobId) {
        return bookmarkRepository.countByJobId(jobId);
    }

    // ── Job View Tracking ─────────────────────────────────────────────────

    @Override
    @Transactional
    public JobView recordView(int jobId, Integer viewerId, String ipAddress, String source) {
        // Deduplicate: same authenticated viewer within last 30 minutes
        if (viewerId != null) {
            LocalDateTime thirtyMinAgo = LocalDateTime.now().minusMinutes(30);
            if (jobViewRepository.existsByJobIdAndViewerIdAndViewedAtAfter(jobId, viewerId, thirtyMinAgo)) {
                return JobView.builder()
                        .jobId(jobId)
                        .viewerId(viewerId)
                        .source("DUPLICATE_SKIP")
                        .build();
            }
        }
        // Persist detailed view event
        JobView view = JobView.builder()
                .jobId(jobId)
                .viewerId(viewerId)
                .ipAddress(ipAddress)
                .source(source != null ? source : "DIRECT")
                .viewedAt(LocalDateTime.now())
                .build();
        // Bump aggregate counter on the Job row
        incrementViewCount(jobId);
        return jobViewRepository.save(view);
    }

    @Override
    public long getViewCount(int jobId) {
        return jobViewRepository.countByJobId(jobId);
    }

    @Override
    public List<JobView> getViewsByJob(int jobId) {
        return jobViewRepository.findByJobId(jobId);
    }

    @Override
    public List<Object[]> getTopViewedJobs() {
        return jobViewRepository.findTopViewedJobs();
    }
}
