package com.hireconnect.notification.repository;

import com.hireconnect.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserId(int userId);

    List<Notification> findByUserIdAndIsRead(int userId, boolean isRead);

    long countByUserIdAndIsRead(int userId, boolean isRead);

    void deleteByNotificationId(int notificationId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(int userId);
}
