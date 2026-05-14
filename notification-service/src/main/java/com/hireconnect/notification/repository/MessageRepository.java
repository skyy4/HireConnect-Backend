package com.hireconnect.notification.repository;

import com.hireconnect.notification.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /** All messages sent by a user */
    List<Message> findBySenderIdOrderBySentAtAsc(int senderId);

    /** All messages received by a user */
    List<Message> findByReceiverIdOrderBySentAtAsc(int receiverId);

    /** Full thread for a specific application (both parties, ordered chronologically) */
    @Query("SELECT m FROM Message m WHERE m.applicationId = :appId ORDER BY m.sentAt ASC")
    List<Message> findThreadByApplicationId(@Param("appId") int applicationId);

    /** Conversation between two specific users */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :u1 AND m.receiverId = :u2) OR (m.senderId = :u2 AND m.receiverId = :u1) " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("u1") int userId1, @Param("u2") int userId2);

    /** Unread count for a receiver */
    long countByReceiverIdAndStatus(int receiverId, String status);

    /** Inbox for a user (all messages received) */
    List<Message> findByReceiverIdAndStatusNotOrderBySentAtDesc(int receiverId, String status);
}
