package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.entity.*;
import com.hireconnect.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscription Service", description = "Recruiter subscription plans, invoices and wallet management")
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;

    // ── Subscription Endpoints ─────────────────────────────────────────────

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to a plan (FREE | PROFESSIONAL | ENTERPRISE)")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Object> body) {
        int recruiterId    = (Integer) body.get("recruiterId");
        String plan        = (String)  body.get("plan");
        String paymentMode = (String)  body.get("paymentMode");
        Double amount      = body.get("amount") != null
                ? ((Number) body.get("amount")).doubleValue() : 0.0;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.subscribe(recruiterId, plan, paymentMode, amount));
    }

    @GetMapping("/subscriptions/recruiter/{recruiterId}/active")
    @Operation(summary = "Get active subscription for a recruiter")
    public ResponseEntity<Subscription> getActive(@PathVariable int recruiterId) {
        try {
            return ResponseEntity.ok(subscriptionService.getActiveSubscription(recruiterId));
        } catch (IllegalStateException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/subscriptions/recruiter/{recruiterId}")
    @Operation(summary = "Get all subscriptions for a recruiter")
    public ResponseEntity<List<Subscription>> getAll(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.getByRecruiter(recruiterId));
    }

    @GetMapping("/subscriptions/admin")
    @Operation(summary = "Get all subscriptions for admin billing visibility")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @PatchMapping("/subscriptions/recruiter/{recruiterId}/cancel")
    @Operation(summary = "Cancel active subscription")
    public ResponseEntity<Subscription> cancel(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(recruiterId));
    }

    @PatchMapping("/subscriptions/recruiter/{recruiterId}/renew")
    @Operation(summary = "Renew subscription with optional plan upgrade")
    public ResponseEntity<Subscription> renew(
            @PathVariable int recruiterId,
            @RequestBody Map<String, Object> body) {
        String plan   = (String) body.get("plan");
        Double amount = ((Number) body.get("amount")).doubleValue();
        return ResponseEntity.ok(subscriptionService.renewSubscription(recruiterId, plan, amount));
    }

    // ── Invoice Endpoints ──────────────────────────────────────────────────

    @GetMapping("/invoices/recruiter/{recruiterId}")
    @Operation(summary = "Get all invoices for a recruiter")
    public ResponseEntity<List<Invoice>> getInvoices(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.getInvoices(recruiterId));
    }

    @GetMapping("/invoices/recruiter/{recruiterId}/latest")
    @Operation(summary = "Get the latest invoice for a recruiter")
    public ResponseEntity<Invoice> getLatestInvoice(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.getLatestInvoice(recruiterId));
    }

    @GetMapping("/invoices/admin")
    @Operation(summary = "Get all invoices for admin billing visibility")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(subscriptionService.getAllInvoices());
    }

    // ── Wallet Endpoints ───────────────────────────────────────────────────

    @PostMapping("/wallet")
    @Operation(summary = "Create a wallet for a user (Candidate or Recruiter)")
    public ResponseEntity<Wallet> createWallet(@RequestBody Map<String, Object> body) {
        int userId     = (Integer) body.get("userId");
        String role    = (String)  body.get("userRole");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.createWallet(userId, role));
    }

    @GetMapping("/wallet/{userId}")
    @Operation(summary = "Get wallet details for a user")
    public ResponseEntity<Wallet> getWallet(@PathVariable int userId) {
        return ResponseEntity.ok(subscriptionService.getWallet(userId));
    }

    @GetMapping("/wallet/{userId}/balance")
    @Operation(summary = "Get current wallet balance for a user")
    public ResponseEntity<Map<String, Double>> getBalance(@PathVariable int userId) {
        return ResponseEntity.ok(Map.of("balance", subscriptionService.getWalletBalance(userId)));
    }

    @PostMapping("/wallet/{userId}/credit")
    @Operation(summary = "Add money to wallet (top-up via card, UPI, net banking)")
    public ResponseEntity<Wallet> credit(
            @PathVariable int userId,
            @RequestBody Map<String, Object> body) {
        Double amount      = ((Number) body.get("amount")).doubleValue();
        String paymentMode = (String) body.getOrDefault("paymentMode", "CARD");
        String txnRef      = (String) body.getOrDefault("transactionRef", null);
        String description = (String) body.getOrDefault("description", "Wallet top-up");
        return ResponseEntity.ok(
                subscriptionService.creditWallet(userId, amount, paymentMode, txnRef, description));
    }

    @PostMapping("/wallet/{userId}/debit")
    @Operation(summary = "Deduct money from wallet (subscription payment, feature purchase)")
    public ResponseEntity<Wallet> debit(
            @PathVariable int userId,
            @RequestBody Map<String, Object> body) {
        Double amount      = ((Number) body.get("amount")).doubleValue();
        String paymentMode = (String) body.getOrDefault("paymentMode", "WALLET");
        String description = (String) body.getOrDefault("description", "Wallet debit");
        return ResponseEntity.ok(
                subscriptionService.debitWallet(userId, amount, paymentMode, description));
    }

    @GetMapping("/wallet/{userId}/transactions")
    @Operation(summary = "Get full wallet transaction history for a user")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable int userId) {
        return ResponseEntity.ok(subscriptionService.getWalletTransactions(userId));
    }

    @GetMapping("/wallet/{userId}/transactions/{type}")
    @Operation(summary = "Get wallet transactions by type: CREDIT | DEBIT")
    public ResponseEntity<List<WalletTransaction>> getTransactionsByType(
            @PathVariable int userId,
            @PathVariable String type) {
        return ResponseEntity.ok(subscriptionService.getWalletTransactionsByType(userId, type));
    }
}
