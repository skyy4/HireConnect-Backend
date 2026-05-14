package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.*;
import com.hireconnect.subscription.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    // ── Subscription lifecycle ─────────────────────────────────────────────

    @Override
    @Transactional
    public Subscription subscribe(int recruiterId, String plan, String paymentMode, Double amount) {
        // Cancel any existing active subscription first
        subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(recruiterId, "ACTIVE")
                .ifPresent(active -> {
                    active.setStatus("CANCELLED");
                    active.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(active);
                });

        Subscription subscription = Subscription.builder()
                .recruiterId(recruiterId)
                .plan(plan.toUpperCase())
                .startDate(LocalDate.now())
                .endDate(calculateEndDate(plan))
                .status("ACTIVE")
                .amountPaid(amount)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        generateInvoice(saved, paymentMode, UUID.randomUUID().toString());
        return saved;
    }

    @Override
    public Subscription getActiveSubscription(int recruiterId) {
        return subscriptionRepository
                .findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(recruiterId, "ACTIVE")
                .orElseThrow(() -> new IllegalStateException("No active subscription for recruiter: " + recruiterId));
    }

    @Override
    public List<Subscription> getByRecruiter(int recruiterId) {
        return subscriptionRepository.findByRecruiterId(recruiterId);
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional
    public Subscription cancelSubscription(int recruiterId) {
        Subscription active = getActiveSubscription(recruiterId);
        active.setStatus("CANCELLED");
        active.setUpdatedAt(LocalDateTime.now());
        return subscriptionRepository.save(active);
    }

    @Override
    @Transactional
    public Subscription renewSubscription(int recruiterId, String plan, Double amount) {
        Subscription active = getActiveSubscription(recruiterId);
        active.setEndDate(LocalDate.now().plusMonths(getPlanDurationMonths(plan)));
        active.setAmountPaid(amount);
        active.setUpdatedAt(LocalDateTime.now());
        Subscription renewed = subscriptionRepository.save(active);
        generateInvoice(renewed, "RENEWAL", UUID.randomUUID().toString());
        return renewed;
    }

    @Override
    @Transactional
    public Invoice generateInvoice(Subscription subscription, String paymentMode, String txnId) {
        Invoice invoice = Invoice.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .recruiterId(subscription.getRecruiterId())
                .amount(subscription.getAmountPaid() != null ? subscription.getAmountPaid() : 0.0)
                .paymentMode(paymentMode)
                .transactionId(txnId)
                .plan(subscription.getPlan())
                .status("PAID")
                .build();
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getInvoices(int recruiterId) {
        return invoiceRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId);
    }

    @Override
    public Invoice getLatestInvoice(int recruiterId) {
        return invoiceRepository.findFirstByRecruiterIdOrderByCreatedAtDesc(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("No invoices found for recruiter: " + recruiterId));
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // ── Wallet management ──────────────────────────────────────────────────

    @Override
    @Transactional
    public Wallet createWallet(int userId, String userRole) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Wallet already exists for userId: " + userId);
        }
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .userRole(userRole.toUpperCase())
                .balance(0.0)
                .currency("INR")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(int userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
    }

    @Override
    @Transactional
    public Wallet creditWallet(int userId, Double amount, String paymentMode, String txnRef, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Credit amount must be positive");
        Wallet wallet = getWallet(userId);
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new IllegalStateException("Wallet is not active: " + wallet.getStatus());
        }
        double newBalance = wallet.getBalance() + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        // Record transaction
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(wallet.getWalletId())
                .userId(userId)
                .type("CREDIT")
                .amount(amount)
                .balanceAfter(newBalance)
                .paymentMode(paymentMode != null ? paymentMode : "CARD")
                .transactionRef(txnRef != null ? txnRef : UUID.randomUUID().toString())
                .description(description != null ? description : "Wallet top-up")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(txn);
        return saved;
    }

    @Override
    @Transactional
    public Wallet debitWallet(int userId, Double amount, String paymentMode, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Debit amount must be positive");
        Wallet wallet = getWallet(userId);
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new IllegalStateException("Wallet is not active: " + wallet.getStatus());
        }
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException(
                    "Insufficient wallet balance. Available: " + wallet.getBalance() + ", Requested: " + amount);
        }
        double newBalance = wallet.getBalance() - amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        // Record transaction
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(wallet.getWalletId())
                .userId(userId)
                .type("DEBIT")
                .amount(amount)
                .balanceAfter(newBalance)
                .paymentMode(paymentMode != null ? paymentMode : "WALLET")
                .transactionRef(UUID.randomUUID().toString())
                .description(description != null ? description : "Wallet debit")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(txn);
        return saved;
    }

    @Override
    public List<WalletTransaction> getWalletTransactions(int userId) {
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<WalletTransaction> getWalletTransactionsByType(int userId, String type) {
        return walletTransactionRepository.findByUserIdAndType(userId, type.toUpperCase());
    }

    @Override
    public Double getWalletBalance(int userId) {
        return getWallet(userId).getBalance();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private LocalDate calculateEndDate(String plan) {
        return LocalDate.now().plusMonths(getPlanDurationMonths(plan));
    }

    private int getPlanDurationMonths(String plan) {
        return switch (plan.toUpperCase()) {
            case "PROFESSIONAL" -> 1;
            case "ENTERPRISE"   -> 12;
            default             -> 0; // FREE plan has no expiry
        };
    }
}
