package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int invoiceId;

    @Column(nullable = false)
    private int subscriptionId;

    @Column(nullable = false)
    private int recruiterId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(length = 30)
    private String paymentMode; // CARD | WALLET | UPI | NET_BANKING

    @Column(length = 100, unique = true)
    private String transactionId;

    @Column(length = 20)
    @Builder.Default
    private String status = "PAID"; // PAID | PENDING | FAILED | REFUNDED

    @Column(length = 30)
    private String plan;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
