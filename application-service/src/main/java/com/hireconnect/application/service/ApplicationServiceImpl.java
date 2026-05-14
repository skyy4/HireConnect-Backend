package com.hireconnect.application.service;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.client.JobServiceClient;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final JobServiceClient jobServiceClient;

    @Override
    @Transactional
    public Application submitApplication(Application application) {
        log.info("Submitting application: candidateId={} jobId={}", application.getCandidateId(), application.getJobId());

        // Synchronous call to job-service to validate job exists
        try {
            jobServiceClient.getJobById(application.getJobId());
        } catch (Exception e) {
            log.warn("Job validation failed for jobId={}: {}", application.getJobId(), e.getMessage());
            throw new IllegalArgumentException("Job not found or unavailable: " + application.getJobId());
        }
        if (applicationRepository.existsByJobIdAndCandidateId(
                application.getJobId(), application.getCandidateId())) {
            log.warn("Duplicate application attempt: candidateId={} jobId={}", application.getCandidateId(), application.getJobId());
            throw new IllegalStateException("Already applied to this job");
        }
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("APPLIED");
        Application saved = applicationRepository.save(application);
        log.info("Application saved: applicationId={} status={}", saved.getApplicationId(), saved.getStatus());

        // Publish event to RabbitMQ for notification-service
        try {
            rabbitTemplate.convertAndSend("hireconnect.notifications",
                    "application.submitted",
                    "APPLICATION:" + saved.getApplicationId() + ":APPLIED:" + saved.getCandidateId());
            log.debug("RabbitMQ event published for applicationId={}", saved.getApplicationId());
        } catch (Exception e) {
            log.warn("Failed to publish RabbitMQ notification for applicationId={}: {}", saved.getApplicationId(), e.getMessage());
            // Non-critical; notification failure should not fail application
        }
        return saved;
    }

    @Override
    public Application getApplicationById(int applicationId) {
        log.debug("Fetching application by id={}", applicationId);
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
    }

    @Override
    public List<Application> getByCandidate(int candidateId) {
        log.debug("Fetching applications for candidateId={}", candidateId);
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Application> getByJob(int jobId) {
        log.debug("Fetching applications for jobId={}", jobId);
        return applicationRepository.findByJobId(jobId);
    }

    @Override
    public List<Application> getByJobAndStatus(int jobId, String status) {
        log.debug("Fetching applications for jobId={} status={}", jobId, status);
        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    @Override
    @Transactional
    public Application updateStatus(int applicationId, String status, String note) {
        log.info("Updating application status: applicationId={} newStatus={}", applicationId, status);
        Application app = getApplicationById(applicationId);
        app.setStatus(status);
        app.setStatusUpdatedAt(LocalDateTime.now());
        if (note != null && !note.isEmpty()) {
            app.setRecruiterNote(note);
        }
        Application updated = applicationRepository.save(app);
        log.info("Application status updated: applicationId={} status={}", applicationId, status);

        // Notify candidate of status change
        try {
            rabbitTemplate.convertAndSend("hireconnect.notifications",
                    "application.status.changed",
                    "APPLICATION:" + applicationId + ":" + status + ":" + app.getCandidateId());
            log.debug("Status change event published for applicationId={}", applicationId);
        } catch (Exception e) {
            log.warn("Failed to publish status change notification for applicationId={}: {}", applicationId, e.getMessage());
            // Non-critical
        }
        return updated;
    }

    @Override
    @Transactional
    public void withdrawApplication(int applicationId, int candidateId) {
        log.info("Withdraw request: applicationId={} candidateId={}", applicationId, candidateId);
        Application app = getApplicationById(applicationId);
        if (app.getCandidateId() != candidateId) {
            log.warn("Unauthorized withdraw attempt: applicationId={} requestedBy={}", applicationId, candidateId);
            throw new SecurityException("Unauthorized to withdraw this application");
        }
        if (app.getStatus().equals("OFFERED") || app.getStatus().equals("HIRED")) {
            log.warn("Cannot withdraw application in status={}", app.getStatus());
            throw new IllegalStateException("Cannot withdraw after offer stage");
        }
        app.setStatus("WITHDRAWN");
        app.setStatusUpdatedAt(LocalDateTime.now());
        applicationRepository.save(app);
        log.info("Application withdrawn: applicationId={}", applicationId);
    }

    @Override
    public boolean hasApplied(int jobId, int candidateId) {
        return applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId);
    }

    @Override
    public long countByJob(int jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    @Override
    public long countByJobAndStatus(int jobId, String status) {
        return applicationRepository.countByJobIdAndStatus(jobId, status);
    }

    @Override
    public long countAll() {
        return applicationRepository.count();
    }
}
