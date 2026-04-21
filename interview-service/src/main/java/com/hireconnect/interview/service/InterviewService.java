package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewService {

    Interview scheduleInterview(Interview interview);

    Interview getInterviewById(int interviewId);

    List<Interview> getByApplicationId(int applicationId);

    List<Interview> getByCandidate(int candidateId);

    List<Interview> getByRecruiter(int recruiterId);

    Interview confirmInterview(int interviewId);

    Interview rescheduleInterview(int interviewId, LocalDateTime newTime, String notes);

    Interview cancelInterview(int interviewId, String reason);

    List<Interview> getByStatus(String status);

    List<Interview> getScheduledBetween(LocalDateTime from, LocalDateTime to);
}
