package com.hireconnect.interview.entity;

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
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int interviewId;

    @Column(nullable = false)
    private int applicationId;

    @Column(nullable = false)
    private int candidateId;

    @Column(nullable = false)
    private int recruiterId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String mode = "ONLINE"; // ONLINE | IN_PERSON

    @Column(length = 500)
    private String meetLink; // for online interviews

    @Column(length = 300)
    private String location; // for in-person

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "SCHEDULED";
    // SCHEDULED | CONFIRMED | RESCHEDULED | CANCELLED | COMPLETED

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String interviewerName;

    @Column(length = 100)
    private String round; // TECHNICAL | HR | MANAGERIAL | FINAL
}
