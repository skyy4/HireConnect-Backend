package com.hireconnect.application.service;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Application submitApplication(Application application) {
        if (applicationRepository.existsByJobIdAndCandidateId(
                application.getJobId(), application.getCandidateId())) {
            throw new IllegalStateException("Already applied to this job");
        }
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("APPLIED");
        Application saved = applicationRepository.save(application);

        // Publish event to RabbitMQ for notification-service
        try {
            rabbitTemplate.convertAndSend("hireconnect.notifications",
                    "application.submitted",
                    "APPLICATION:" + saved.getApplicationId() + ":APPLIED:" + saved.getCandidateId());
        } catch (Exception e) {
            // Non-critical; notification failure should not fail application
        }
        return saved;
    }

    @Override
    public Application getApplicationById(int applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
    }

    @Override
    public List<Application> getByCandidate(int candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Application> getByJob(int jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    @Override
    public List<Application> getByJobAndStatus(int jobId, String status) {
        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    @Override
    @Transactional
    public Application updateStatus(int applicationId, String status, String note) {
        Application app = getApplicationById(applicationId);
        app.setStatus(status);
        app.setStatusUpdatedAt(LocalDateTime.now());
        if (note != null && !note.isEmpty()) {
            app.setRecruiterNote(note);
        }
        Application updated = applicationRepository.save(app);

        // Notify candidate of status change
        try {
            rabbitTemplate.convertAndSend("hireconnect.notifications",
                    "application.status.changed",
                    "APPLICATION:" + applicationId + ":" + status + ":" + app.getCandidateId());
        } catch (Exception e) {
            // Non-critical
        }
        return updated;
    }

    @Override
    @Transactional
    public void withdrawApplication(int applicationId, int candidateId) {
        Application app = getApplicationById(applicationId);
        if (app.getCandidateId() != candidateId) {
            throw new SecurityException("Unauthorized to withdraw this application");
        }
        if (app.getStatus().equals("OFFERED") || app.getStatus().equals("HIRED")) {
            throw new IllegalStateException("Cannot withdraw after offer stage");
        }
        app.setStatus("WITHDRAWN");
        app.setStatusUpdatedAt(LocalDateTime.now());
        applicationRepository.save(app);
    }

    @Override
    public boolean hasApplied(int jobId, int candidateId) {
        return applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId);
    }

    @Override
    public long countByJob(int jobId) {
        return applicationRepository.countByJobId(jobId);
    }
}
