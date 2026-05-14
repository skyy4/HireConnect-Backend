package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Records every credit or debit transaction on a user's Wallet.
 * Provides a full audit trail for the recruiter billing dashboard
 * and candidate account statements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wt_wallet", columnList = "wallet_id"),
        @Index(name = "idx_wt_user",   columnList = "user_id")
})
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    @Column(nullable = false, name = "wallet_id")
    private int walletId;

    @Column(nullable = false, name = "user_id")
    private int userId;

    /** CREDIT | DEBIT */
    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private Double amount;

    /** Balance after this transaction */
    @Column(nullable = false)
    private Double balanceAfter;

    /**
     * Payment mode for CREDIT transactions:
     * WALLET | CARD | UPI | NET_BANKING | REFUND
     * For DEBIT: SUBSCRIPTION | RESUME_BOOST | PRIORITY_APPLY | WITHDRAWAL
     */
    @Column(nullable = false, length = 30)
    private String paymentMode;

    /** External payment gateway transaction ID */
    @Column(length = 100)
    private String transactionRef;

    /** Human-readable description */
    @Column(length = 300)
    private String description;

    /** SUCCESS | FAILED | REFUNDED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
