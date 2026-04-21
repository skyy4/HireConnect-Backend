package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int subscriptionId;

    @Column(nullable = false)
    private int recruiterId;

    @Column(nullable = false, length = 30)
    private String plan; // FREE | PROFESSIONAL | ENTERPRISE

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE | CANCELLED | EXPIRED

    private Double amountPaid;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ── helper ───────────────────────────────────────────────────────────────
    public boolean isActive() {
        return "ACTIVE".equals(status) &&
               (endDate == null || !endDate.isBefore(LocalDate.now()));
    }
}
