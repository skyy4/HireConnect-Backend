package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Message;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.repository.MessageRepository;
import com.hireconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // ── Notification Tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("sendNotification — saves with isRead=false and timestamp")
    void sendNotification_savesCorrectly() {
        Notification notif = Notification.builder()
                .userId(1)
                .type("TEST")
                .message("Hello")
                .build();
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.sendNotification(notif);

        assertThat(result.isRead()).isFalse();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsRead — sets isRead=true")
    void markAsRead_setsRead() {
        Notification notif = Notification.builder().userId(1).type("T").message("M").build();
        notif.setRead(false);

        when(notificationRepository.findById(1)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.markAsRead(1);
        assertThat(result.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead — throws when notification not found")
    void markAsRead_notFound_throws() {
        when(notificationRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.markAsRead(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    @DisplayName("getUnreadByUser — returns unread notifications")
    void getUnreadByUser_returnsUnread() {
        List<Notification> unread = List.of(
                Notification.builder().userId(5).type("T").message("Msg").build()
        );
        when(notificationRepository.findByUserIdAndIsRead(5, false)).thenReturn(unread);

        assertThat(notificationService.getUnreadByUser(5)).hasSize(1);
    }

    @Test
    @DisplayName("countUnread — returns count from repository")
    void countUnread_returnsCount() {
        when(notificationRepository.countByUserIdAndIsRead(5, false)).thenReturn(3L);
        assertThat(notificationService.countUnread(5)).isEqualTo(3L);
    }

    @Test
    @DisplayName("deleteNotification — calls deleteById")
    void deleteNotification_callsRepo() {
        doNothing().when(notificationRepository).deleteById(anyInt());
        notificationService.deleteNotification(10);
        verify(notificationRepository).deleteById(10);
    }

    // ── Messaging Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("sendMessage — saves message and creates in-app notification")
    void sendMessage_savesAndNotifies() {
        Message msg = Message.builder()
                .senderId(1)
                .receiverId(2)
                .content("Hello!")
                .build();
        Message saved = Message.builder()
                .messageId(10)
                .senderId(1)
                .receiverId(2)
                .content("Hello!")
                .status("SENT")
                .build();

        when(messageRepository.save(any())).thenReturn(saved);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Message result = notificationService.sendMessage(msg);
        assertThat(result.getStatus()).isEqualTo("SENT");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("markMessageRead — updates status to READ")
    void markMessageRead_updatesStatus() {
        Message msg = Message.builder().messageId(1).senderId(1).receiverId(2).status("SENT").build();
        when(messageRepository.findById(1)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Message result = notificationService.markMessageRead(1);
        assertThat(result.getStatus()).isEqualTo("READ");
    }

    @Test
    @DisplayName("sendEmailAlert — calls JavaMailSender")
    void sendEmailAlert_callsMailSender() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        notificationService.sendEmailAlert("test@x.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("markAllRead — marks all user notifications as read")
    void markAllRead_marksAll() {
        Notification n1 = Notification.builder().userId(5).type("T").message("M").build();
        Notification n2 = Notification.builder().userId(5).type("T2").message("M2").build();

        when(notificationRepository.findByUserIdAndIsRead(5, false)).thenReturn(List.of(n1, n2));
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(n1, n2));

        notificationService.markAllRead(5);
        verify(notificationRepository).saveAll(anyList());
    }

    // ── RabbitMQ Consumer Test ─────────────────────────────────────────────

    @Test
    @DisplayName("handleNotificationEvent — processes APPLICATION event without exception")
    void handleNotificationEvent_applicationEvent_noException() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                notificationService.handleNotificationEvent("APPLICATION:42:SHORTLISTED:101"));
    }

    @Test
    @DisplayName("handleNotificationEvent — processes INTERVIEW event without exception")
    void handleNotificationEvent_interviewEvent_noException() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                notificationService.handleNotificationEvent("INTERVIEW:1:SCHEDULED:101:201:2026-06-01T10:00"));
    }

    @Test
    @DisplayName("handleNotificationEvent — handles malformed event gracefully")
    void handleNotificationEvent_malformed_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                notificationService.handleNotificationEvent("UNKNOWN_EVENT"));
    }
}
