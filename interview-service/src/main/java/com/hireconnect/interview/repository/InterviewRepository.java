package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {

    List<Interview> findByApplicationId(int applicationId);

    List<Interview> findByStatus(String status);

    List<Interview> findByScheduledAtBetween(LocalDateTime from, LocalDateTime to);

    void deleteByInterviewId(int interviewId);

    List<Interview> findByCandidateId(int candidateId);

    List<Interview> findByRecruiterId(int recruiterId);

    List<Interview> findByMode(String mode);
}
