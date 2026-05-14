package com.hireconnect.notification.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.notification.entity.Message;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationResource Controller Tests")
class NotificationResourceTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Mock private NotificationService notificationService;
    @InjectMocks private NotificationResource notificationResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationResource).build();
    }

    private Notification buildNotif(int id, boolean read) {
        Notification n = new Notification();
        n.setNotificationId(id);
        n.setUserId(1);
        n.setType("APPLICATION_STATUS");
        n.setMessage("Your application was shortlisted");
        n.setRead(read);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    private Message buildMessage(int id, String status) {
        return Message.builder()
                .messageId(id)
                .senderId(1)
                .receiverId(2)
                .content("Hello candidate!")
                .status(status)
                .sentAt(LocalDateTime.now())
                .build();
    }

    // ── Notification Endpoints ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/notifications/user/{userId} — returns all notifications")
    void getByUser_returns200() throws Exception {
        // Correct method: getByUser(int userId)
        when(notificationService.getByUser(1))
                .thenReturn(List.of(buildNotif(1, false), buildNotif(2, true)));

        mockMvc.perform(get("/api/v1/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/user/{userId}/unread — returns unread only")
    void getUnread_returns200() throws Exception {
        when(notificationService.getUnreadByUser(1))
                .thenReturn(List.of(buildNotif(1, false)));

        mockMvc.perform(get("/api/v1/notifications/user/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/user/{userId}/count — returns unread count")
    void countUnread_returns200() throws Exception {
        when(notificationService.countUnread(1)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/user/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(5)));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read — marks as read returns 200")
    void markRead_returns200() throws Exception {
        Notification n = buildNotif(1, true);
        when(notificationService.markAsRead(1)).thenReturn(n);

        mockMvc.perform(patch("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/user/{id}/read-all — marks all read")
    void markAllRead_returns200() throws Exception {
        doNothing().when(notificationService).markAllRead(1);

        mockMvc.perform(patch("/api/v1/notifications/user/1/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("read")));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} — deletes notification")
    void deleteNotification_returns200() throws Exception {
        doNothing().when(notificationService).deleteNotification(1);

        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted")));
    }

    // ── Message Endpoints (/api/v1/messages) ──────────────────────────────

    @Test
    @DisplayName("POST /api/v1/messages — sends message and returns 201")
    void sendMessage_returns201() throws Exception {
        Message saved = buildMessage(1, "SENT");
        when(notificationService.sendMessage(any(Message.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildMessage(0, "SENT"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId", is(1)))
                .andExpect(jsonPath("$.status", is("SENT")));
    }

    @Test
    @DisplayName("GET /api/v1/messages/conversation — returns conversation between two users")
    void getConversation_returns200() throws Exception {
        // Correct method: getConversation(int userId1, int userId2)
        when(notificationService.getConversation(1, 2))
                .thenReturn(List.of(buildMessage(1, "READ"), buildMessage(2, "SENT")));

        mockMvc.perform(get("/api/v1/messages/conversation")
                        .param("userId1", "1")
                        .param("userId2", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PATCH /api/v1/messages/{id}/read — marks message as read")
    void markMessageRead_returns200() throws Exception {
        Message read = buildMessage(1, "READ");
        when(notificationService.markMessageRead(1)).thenReturn(read);

        mockMvc.perform(patch("/api/v1/messages/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READ")));
    }

    @Test
    @DisplayName("GET /api/v1/messages/inbox/{userId} — returns inbox messages")
    void getInbox_returns200() throws Exception {
        when(notificationService.getInbox(1))
                .thenReturn(List.of(buildMessage(1, "SENT"), buildMessage(2, "READ")));

        mockMvc.perform(get("/api/v1/messages/inbox/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/messages/unread/count/{userId} — returns unread message count")
    void countUnreadMessages_returns200() throws Exception {
        when(notificationService.countUnreadMessages(1)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/messages/unread/count/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadMessages", is(3)));
    }

    @Test
    @DisplayName("DELETE /api/v1/messages/{id} — deletes message")
    void deleteMessage_returns200() throws Exception {
        doNothing().when(notificationService).deleteMessage(1);

        mockMvc.perform(delete("/api/v1/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted")));
    }
}
