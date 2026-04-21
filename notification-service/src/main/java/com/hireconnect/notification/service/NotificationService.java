package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification sendNotification(Notification notification);

    Notification markAsRead(int notificationId);

    void markAllRead(int userId);

    List<Notification> getByUser(int userId);

    List<Notification> getUnreadByUser(int userId);

    long countUnread(int userId);

    void deleteNotification(int notificationId);

    void sendEmailAlert(String email, String subject, String body);
}
