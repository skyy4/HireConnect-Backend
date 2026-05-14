package com.hireconnect.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a direct message sent from a Recruiter to a shortlisted Candidate
 * (or vice versa) through the HireConnect portal.
 *
 * Threads are identified by the (applicationId) pair so both parties
 * can view the full conversation history within a job application context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_msg_sender",    columnList = "sender_id"),
        @Index(name = "idx_msg_receiver",  columnList = "receiver_id"),
        @Index(name = "idx_msg_app",       columnList = "application_id")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId;

    /** userId of the message sender (Recruiter or Candidate) */
    @Column(nullable = false, name = "sender_id")
    private int senderId;

    /** userId of the message recipient */
    @Column(nullable = false, name = "receiver_id")
    private int receiverId;

    /**
     * Optional: links the message to a specific job application thread.
     * Null for general (non-application-specific) conversations.
     */
    @Column(name = "application_id")
    private Integer applicationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** SENT | DELIVERED | READ */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SENT";

    /** MESSAGE | INTERVIEW_INVITE | OFFER_LETTER | GENERAL */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String messageType = "MESSAGE";

    /** Optional file attachment URL (offer letter PDF, etc.) */
    @Column(length = 500)
    private String attachmentUrl;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    private LocalDateTime readAt;
}
