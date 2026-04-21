package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;

    @Override
    @Transactional
    public Interview scheduleInterview(Interview interview) {
        interview.setStatus("SCHEDULED");
        interview.setCreatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
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
        return interviewRepository.save(interview);
    }

    @Override
    @Transactional
    public Interview rescheduleInterview(int interviewId, LocalDateTime newTime, String notes) {
        Interview interview = getInterviewById(interviewId);
        interview.setScheduledAt(newTime);
        interview.setStatus("RESCHEDULED");
        interview.setUpdatedAt(LocalDateTime.now());
        if (notes != null) interview.setNotes(notes);
        return interviewRepository.save(interview);
    }

    @Override
    @Transactional
    public Interview cancelInterview(int interviewId, String reason) {
        Interview interview = getInterviewById(interviewId);
        interview.setStatus("CANCELLED");
        interview.setNotes(reason);
        interview.setUpdatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    @Override
    public List<Interview> getByStatus(String status) {
        return interviewRepository.findByStatus(status);
    }

    @Override
    public List<Interview> getScheduledBetween(LocalDateTime from, LocalDateTime to) {
        return interviewRepository.findByScheduledAtBetween(from, to);
    }
}
