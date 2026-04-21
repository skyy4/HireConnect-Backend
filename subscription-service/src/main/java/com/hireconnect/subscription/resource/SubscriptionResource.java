package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscription Service", description = "Recruiter subscription plans and invoices")
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;

    // ── Subscription endpoints ─────────────────────────────────────────────
    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to a plan (Recruiter)")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Object> body) {
        int recruiterId = (Integer) body.get("recruiterId");
        String plan = (String) body.get("plan");
        String paymentMode = (String) body.get("paymentMode");
        Double amount = body.get("amount") != null ? ((Number) body.get("amount")).doubleValue() : 0.0;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.subscribe(recruiterId, plan, paymentMode, amount));
    }

    @GetMapping("/subscriptions/recruiter/{recruiterId}/active")
    @Operation(summary = "Get active subscription for a recruiter")
    public ResponseEntity<Subscription> getActive(@PathVariable int recruiterId) {
        try {
            return ResponseEntity.ok(subscriptionService.getActiveSubscription(recruiterId));
        } catch (IllegalStateException e) {
            // Return 204 No Content instead of a 500 error if there isn't an active subscription
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/subscriptions/recruiter/{recruiterId}")
    @Operation(summary = "Get all subscriptions for a recruiter")
    public ResponseEntity<List<Subscription>> getAll(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.getByRecruiter(recruiterId));
    }

    @PatchMapping("/subscriptions/recruiter/{recruiterId}/cancel")
    @Operation(summary = "Cancel active subscription (Recruiter)")
    public ResponseEntity<Subscription> cancel(@PathVariable int recruiterId) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(recruiterId));
    }

    @PatchMapping("/subscriptions/recruiter/{recruiterId}/renew")
    @Operation(summary = "Renew subscription (Recruiter)")
    public ResponseEntity<Subscription> renew(
            @PathVariable int recruiterId,
            @RequestBody Map<String, Object> body) {
        String plan = (String) body.get("plan");
        Double amount = ((Number) body.get("amount")).doubleValue();
        return ResponseEntity.ok(subscriptionService.renewSubscription(recruiterId, plan, amount));
    }

    // ── Invoice endpoints ──────────────────────────────────────────────────
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
}
