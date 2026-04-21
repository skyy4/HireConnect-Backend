package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public Subscription subscribe(int recruiterId, String plan, String paymentMode, Double amount) {
        // Cancel any existing active subscription
        subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(recruiterId, "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    existing.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(existing);
                });

        Subscription subscription = Subscription.builder()
                .recruiterId(recruiterId)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(calculateEndDate(plan))
                .status("ACTIVE")
                .amountPaid(amount)
                .build();
        Subscription saved = subscriptionRepository.save(subscription);

        // Generate invoice
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

    // ── helpers ──────────────────────────────────────────────────────────────
    private LocalDate calculateEndDate(String plan) {
        return LocalDate.now().plusMonths(getPlanDurationMonths(plan));
    }

    private int getPlanDurationMonths(String plan) {
        return switch (plan.toUpperCase()) {
            case "PROFESSIONAL" -> 1;
            case "ENTERPRISE" -> 12;
            default -> 0; // FREE plan has no expiry
        };
    }
}
