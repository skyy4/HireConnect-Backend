package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Message;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.repository.MessageRepository;
import com.hireconnect.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final MessageRepository messageRepository;
    private final JavaMailSender mailSender;

    // ── Notifications ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification sendNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(int userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public List<Notification> getByUser(int userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadByUser(int userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    @Override
    public long countUnread(int userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public void deleteNotification(int notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void sendEmailAlert(String email, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@hireconnect.com");
            mailSender.send(message);
            log.info("Email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
        }
    }

    // ── RabbitMQ Listener ────────────────────────────────────────────────────

    @RabbitListener(queues = "hireconnect.notifications")
    public void handleNotificationEvent(String message) {
        log.info("Received notification event: {}", message);
        try {
            String[] parts = message.split(":");
            if (parts.length < 2) return;

            switch (parts[0]) {
                case "APPLICATION" -> handleApplicationEvent(parts);
                case "INTERVIEW" -> handleInterviewEvent(parts);
                case "JOB_POSTED" -> handleNewJobEvent(parts);
                default -> log.warn("Unknown event type: {}", parts[0]);
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage());
        }
    }

    private void handleApplicationEvent(String[] parts) {
        if (parts.length >= 4) {
            int candidateId = Integer.parseInt(parts[3]);
            String status = parts[2];
            String notifMessage = buildStatusMessage(status, parts[1]);
            Notification notification = Notification.builder()
                    .userId(candidateId)
                    .type("APPLICATION_STATUS")
                    .message(notifMessage)
                    .referenceId(parts[1])
                    .build();
            sendNotification(notification);
        }
    }

    private void handleInterviewEvent(String[] parts) {
        // Format: INTERVIEW:<interviewId>:<action>:<candidateId>:<recruiterId>:<scheduledAt>
        if (parts.length >= 5) {
            int candidateId = Integer.parseInt(parts[3]);
            int recruiterId = Integer.parseInt(parts[4]);
            String action = parts[2];
            String scheduledAt = parts.length >= 6 ? parts[5] : "";
            String interviewId = parts[1];

            // Notify candidate
            String candidateMsg = buildInterviewMessage(action, interviewId, scheduledAt);
            sendNotification(Notification.builder()
                    .userId(candidateId)
                    .type("INTERVIEW_" + action)
                    .message(candidateMsg)
                    .referenceId(interviewId)
                    .actionUrl("/interviews")
                    .build());

            // Notify recruiter for confirmations
            if ("CONFIRMED".equals(action)) {
                sendNotification(Notification.builder()
                        .userId(recruiterId)
                        .type("INTERVIEW_CONFIRMED")
                        .message("Candidate has confirmed interview #" + interviewId)
                        .referenceId(interviewId)
                        .actionUrl("/recruiter/interviews")
                        .build());
            }
        }
    }

    private void handleNewJobEvent(String[] parts) {
        // Format: JOB_POSTED:<jobId>:<title>:<category>:<location>
        if (parts.length >= 3) {
            String jobId = parts[1];
            String title = parts[2];
            String category = parts.length >= 4 ? parts[3] : "";
            log.info("New job posted: {} - {} ({})", jobId, title, category);
            // Note: In a production system, this would query candidate profiles
            // to find matching skills/preferences and notify only relevant candidates.
            // For now, we log the event — the notification will be created when
            // candidates browse matching job categories.
        }
    }

    private String buildInterviewMessage(String action, String interviewId, String scheduledAt) {
        return switch (action) {
            case "SCHEDULED" -> "An interview has been scheduled for you (Interview #" + interviewId + ")" +
                    (scheduledAt.isEmpty() ? "" : " at " + scheduledAt);
            case "CONFIRMED" -> "Your interview #" + interviewId + " has been confirmed.";
            case "RESCHEDULED" -> "Your interview #" + interviewId + " has been rescheduled" +
                    (scheduledAt.isEmpty() ? "." : " to " + scheduledAt + ".");
            case "CANCELLED" -> "Your interview #" + interviewId + " has been cancelled.";
            default -> "Interview #" + interviewId + " status updated to: " + action;
        };
    }

    private String buildStatusMessage(String status, String applicationId) {
        return switch (status) {
            case "SHORTLISTED" -> "Congratulations! You have been shortlisted for application #" + applicationId;
            case "INTERVIEW_SCHEDULED" -> "An interview has been scheduled for application #" + applicationId;
            case "OFFERED" -> "You have received a job offer for application #" + applicationId + "!";
            case "REJECTED" -> "We regret to inform you that application #" + applicationId + " was not successful.";
            default -> "Your application #" + applicationId + " status has been updated to: " + status;
        };
    }

    // ── Messaging (Recruiter ↔ Candidate) ─────────────────────────────────

    @Override
    @Transactional
    public Message sendMessage(Message message) {
        message.setSentAt(LocalDateTime.now());
        message.setStatus("SENT");
        Message saved = messageRepository.save(message);
        // Also create an in-app notification for the receiver
        Notification inAppNotif = Notification.builder()
                .userId(message.getReceiverId())
                .type("MESSAGE")
                .message("You have a new message from user #" + message.getSenderId())
                .referenceId(String.valueOf(saved.getMessageId()))
                .actionUrl("/messages/" + saved.getMessageId())
                .build();
        sendNotification(inAppNotif);
        log.info("Message sent from userId={} to userId={}", message.getSenderId(), message.getReceiverId());
        return saved;
    }

    @Override
    public Message getMessageById(int messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
    }

    @Override
    public List<Message> getConversation(int userId1, int userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    @Override
    public List<Message> getApplicationThread(int applicationId) {
        return messageRepository.findThreadByApplicationId(applicationId);
    }

    @Override
    public List<Message> getInbox(int userId) {
        return messageRepository.findByReceiverIdOrderBySentAtAsc(userId);
    }

    @Override
    public List<Message> getSentMessages(int userId) {
        return messageRepository.findBySenderIdOrderBySentAtAsc(userId);
    }

    @Override
    @Transactional
    public Message markMessageRead(int messageId) {
        Message msg = getMessageById(messageId);
        msg.setStatus("READ");
        msg.setReadAt(LocalDateTime.now());
        return messageRepository.save(msg);
    }

    @Override
    public long countUnreadMessages(int userId) {
        return messageRepository.countByReceiverIdAndStatus(userId, "SENT");
    }

    @Override
    @Transactional
    public void deleteMessage(int messageId) {
        messageRepository.deleteById(messageId);
    }
}
