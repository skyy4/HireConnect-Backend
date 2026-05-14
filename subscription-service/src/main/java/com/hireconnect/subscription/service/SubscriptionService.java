package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.Wallet;
import com.hireconnect.subscription.entity.WalletTransaction;

import java.util.List;

public interface SubscriptionService {

    // ── Subscription lifecycle ─────────────────────────────────────────────
    Subscription subscribe(int recruiterId, String plan, String paymentMode, Double amount);

    Subscription getActiveSubscription(int recruiterId);

    List<Subscription> getByRecruiter(int recruiterId);

    List<Subscription> getAllSubscriptions();

    Subscription cancelSubscription(int recruiterId);

    Subscription renewSubscription(int recruiterId, String plan, Double amount);

    Invoice generateInvoice(Subscription subscription, String paymentMode, String txnId);

    List<Invoice> getInvoices(int recruiterId);

    Invoice getLatestInvoice(int recruiterId);

    List<Invoice> getAllInvoices();

    // ── Wallet management ──────────────────────────────────────────────────
    Wallet createWallet(int userId, String userRole);

    Wallet getWallet(int userId);

    Wallet creditWallet(int userId, Double amount, String paymentMode, String txnRef, String description);

    Wallet debitWallet(int userId, Double amount, String paymentMode, String description);

    List<WalletTransaction> getWalletTransactions(int userId);

    List<WalletTransaction> getWalletTransactionsByType(int userId, String type);

    Double getWalletBalance(int userId);
}
