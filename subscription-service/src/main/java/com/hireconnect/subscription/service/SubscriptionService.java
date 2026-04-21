package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;

import java.util.List;

public interface SubscriptionService {

    Subscription subscribe(int recruiterId, String plan, String paymentMode, Double amount);

    Subscription getActiveSubscription(int recruiterId);

    List<Subscription> getByRecruiter(int recruiterId);

    Subscription cancelSubscription(int recruiterId);

    Subscription renewSubscription(int recruiterId, String plan, Double amount);

    Invoice generateInvoice(Subscription subscription, String paymentMode, String txnId);

    List<Invoice> getInvoices(int recruiterId);

    Invoice getLatestInvoice(int recruiterId);
}
