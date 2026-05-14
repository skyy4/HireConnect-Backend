package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wallet for a candidate user. Candidates can add money and use it
 * as a payment mode for premium features (e.g., resume boosting,
 * priority application, or premium job alerts).
 *
 * Recruiters also use wallet for quick subscription renewals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int walletId;

    /** userId from auth-service (Candidate or Recruiter) */
    @Column(nullable = false, unique = true)
    private int userId;

    /** Role of the wallet owner: CANDIDATE | RECRUITER */
    @Column(nullable = false, length = 20)
    private String userRole;

    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.0;

    /** Currency code  e.g. INR, USD */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    /** ACTIVE | FROZEN | CLOSED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
