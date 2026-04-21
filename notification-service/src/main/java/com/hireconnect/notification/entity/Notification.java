package com.hireconnect.notification.entity;

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
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationId;

    @Column(nullable = false)
    private int userId;

    @Column(nullable = false, length = 50)
    private String type;
    // APPLICATION_STATUS | INTERVIEW_SCHEDULED | NEW_JOB_ALERT | MESSAGE | SYSTEM

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 200)
    private String actionUrl; // deep link to relevant resource

    @Column(length = 50)
    private String referenceId; // jobId, applicationId, interviewId etc.
}
