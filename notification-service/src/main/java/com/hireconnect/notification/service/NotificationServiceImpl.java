package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Notification;
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
    private final JavaMailSender mailSender;

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
            // Format: "APPLICATION:{id}:{status}:{candidateId}"
            String[] parts = message.split(":");
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
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage());
        }
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
}
