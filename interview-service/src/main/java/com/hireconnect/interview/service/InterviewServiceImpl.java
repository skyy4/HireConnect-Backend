package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);
    private static final String NOTIFICATION_EXCHANGE = "hireconnect.notifications";

    private final InterviewRepository interviewRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Interview scheduleInterview(Interview interview) {
        interview.setStatus("SCHEDULED");
        interview.setCreatedAt(LocalDateTime.now());
        Interview saved = interviewRepository.save(interview);
        publishEvent(saved, "SCHEDULED");
        return saved;
    }

    @Override
    public Interview getInterviewById(int interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));
    }

    @Override
    public List<Interview> getByApplicationId(int applicationId) {
        return interviewRepository.findByApplicationId(applicationId);
    }

    @Override
    public List<Interview> getByCandidate(int candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Interview> getByRecruiter(int recruiterId) {
        return interviewRepository.findByRecruiterId(recruiterId);
    }

    @Override
    @Transactional
    public Interview confirmInterview(int interviewId) {
        Interview interview = getInterviewById(interviewId);
        interview.setStatus("CONFIRMED");
        interview.setUpdatedAt(LocalDateTime.now());
        Interview saved = interviewRepository.save(interview);
        publishEvent(saved, "CONFIRMED");
        return saved;
    }

    @Override
    @Transactional
    public Interview rescheduleInterview(int interviewId, LocalDateTime newTime, String notes) {
        Interview interview = getInterviewById(interviewId);
        interview.setScheduledAt(newTime);
        interview.setStatus("RESCHEDULED");
        interview.setUpdatedAt(LocalDateTime.now());
        if (notes != null) interview.setNotes(notes);
        Interview saved = interviewRepository.save(interview);
        publishEvent(saved, "RESCHEDULED");
        return saved;
    }

    @Override
    @Transactional
    public Interview cancelInterview(int interviewId, String reason) {
        Interview interview = getInterviewById(interviewId);
        interview.setStatus("CANCELLED");
        interview.setNotes(reason);
        interview.setUpdatedAt(LocalDateTime.now());
        Interview saved = interviewRepository.save(interview);
        publishEvent(saved, "CANCELLED");
        return saved;
    }

    @Override
    public List<Interview> getByStatus(String status) {
        return interviewRepository.findByStatus(status);
    }

    @Override
    public List<Interview> getScheduledBetween(LocalDateTime from, LocalDateTime to) {
        return interviewRepository.findByScheduledAtBetween(from, to);
    }

    // ── RabbitMQ Event Publishing ──────────────────────────────────────────
    private void publishEvent(Interview interview, String action) {
        try {
            // Format: INTERVIEW:<interviewId>:<action>:<candidateId>:<recruiterId>:<scheduledAt>
            String message = String.format("INTERVIEW:%d:%s:%d:%d:%s",
                    interview.getInterviewId(),
                    action,
                    interview.getCandidateId(),
                    interview.getRecruiterId(),
                    interview.getScheduledAt() != null ? interview.getScheduledAt().toString() : "");
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "", message);
            log.info("Published interview event: {}", message);
        } catch (Exception ex) {
            log.warn("Failed to publish interview event: {}", ex.getMessage());
        }
    }
}
