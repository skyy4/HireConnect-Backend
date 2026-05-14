package com.hireconnect.notification.resource;

import com.hireconnect.notification.entity.Message;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Notification Service", description = "In-app notifications and recruiter-to-candidate messaging")
public class NotificationResource {

    private final NotificationService notificationService;

    // ── Notification Endpoints ─────────────────────────────────────────────

    @PostMapping("/api/v1/notifications")
    @Operation(summary = "Send an in-app notification")
    public ResponseEntity<Notification> send(@RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendNotification(notification));
    }

    @GetMapping("/api/v1/notifications/user/{userId}")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @GetMapping("/api/v1/notifications/user/{userId}/unread")
    @Operation(summary = "Get unread notifications for a user")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getUnreadByUser(userId));
    }

    @GetMapping("/api/v1/notifications/user/{userId}/count")
    @Operation(summary = "Count unread notifications for a user")
    public ResponseEntity<Map<String, Long>> countUnread(@PathVariable int userId) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    @PatchMapping("/api/v1/notifications/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Notification> markRead(@PathVariable int notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PatchMapping("/api/v1/notifications/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read for a user")
    public ResponseEntity<Map<String, String>> markAllRead(@PathVariable int userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/api/v1/notifications/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable int notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    // ── Messaging Endpoints ────────────────────────────────────────────────

    @PostMapping("/api/v1/messages")
    @Operation(summary = "Send a message (Recruiter → Candidate or vice versa)")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody Message message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendMessage(message));
    }

    @GetMapping("/api/v1/messages/{messageId}")
    @Operation(summary = "Get a specific message by ID")
    public ResponseEntity<Message> getMessage(@PathVariable int messageId) {
        return ResponseEntity.ok(notificationService.getMessageById(messageId));
    }

    @GetMapping("/api/v1/messages/conversation")
    @Operation(summary = "Get full conversation between two users")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam int userId1,
            @RequestParam int userId2) {
        return ResponseEntity.ok(notificationService.getConversation(userId1, userId2));
    }

    @GetMapping("/api/v1/messages/application/{applicationId}")
    @Operation(summary = "Get all messages in an application thread")
    public ResponseEntity<List<Message>> getApplicationThread(@PathVariable int applicationId) {
        return ResponseEntity.ok(notificationService.getApplicationThread(applicationId));
    }

    @GetMapping("/api/v1/messages/inbox/{userId}")
    @Operation(summary = "Get inbox (received messages) for a user")
    public ResponseEntity<List<Message>> getInbox(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getInbox(userId));
    }

    @GetMapping("/api/v1/messages/sent/{userId}")
    @Operation(summary = "Get sent messages for a user")
    public ResponseEntity<List<Message>> getSent(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getSentMessages(userId));
    }

    @PatchMapping("/api/v1/messages/{messageId}/read")
    @Operation(summary = "Mark a message as read")
    public ResponseEntity<Message> markMessageRead(@PathVariable int messageId) {
        return ResponseEntity.ok(notificationService.markMessageRead(messageId));
    }

    @GetMapping("/api/v1/messages/unread/count/{userId}")
    @Operation(summary = "Count unread messages for a user")
    public ResponseEntity<Map<String, Long>> countUnreadMessages(@PathVariable int userId) {
        return ResponseEntity.ok(Map.of("unreadMessages", notificationService.countUnreadMessages(userId)));
    }

    @DeleteMapping("/api/v1/messages/{messageId}")
    @Operation(summary = "Delete a message")
    public ResponseEntity<Map<String, String>> deleteMessage(@PathVariable int messageId) {
        notificationService.deleteMessage(messageId);
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }
}
