package com.hireconnect.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "candidate_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int applicationId;

    @Column(name = "job_id", nullable = false)
    private int jobId;

    @Column(name = "candidate_id", nullable = false)
    private int candidateId; // userId from auth-service

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "APPLIED";
    // APPLIED | SHORTLISTED | INTERVIEW_SCHEDULED | OFFERED | REJECTED | WITHDRAWN

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(length = 500)
    private String resumeUrl;

    private LocalDateTime statusUpdatedAt;

    @Column(columnDefinition = "TEXT")
    private String recruiterNote; // internal recruiter notes
}
