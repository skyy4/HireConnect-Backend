package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Message;
import com.hireconnect.notification.entity.Notification;

import java.util.List;

public interface NotificationService {

    // ── Notifications ──────────────────────────────────────────────────────
    Notification sendNotification(Notification notification);

    Notification markAsRead(int notificationId);

    void markAllRead(int userId);

    List<Notification> getByUser(int userId);

    List<Notification> getUnreadByUser(int userId);

    long countUnread(int userId);

    void deleteNotification(int notificationId);

    void sendEmailAlert(String email, String subject, String body);

    // ── Messaging (Recruiter ↔ Candidate) ─────────────────────────────────
    Message sendMessage(Message message);

    Message getMessageById(int messageId);

    List<Message> getConversation(int userId1, int userId2);

    List<Message> getApplicationThread(int applicationId);

    List<Message> getInbox(int userId);

    List<Message> getSentMessages(int userId);

    Message markMessageRead(int messageId);

    long countUnreadMessages(int userId);

    void deleteMessage(int messageId);
}
