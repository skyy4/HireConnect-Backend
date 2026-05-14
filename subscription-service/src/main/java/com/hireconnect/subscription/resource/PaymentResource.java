package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.service.RazorpayService;
import com.hireconnect.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Razorpay Payments", description = "Create orders and verify payments via Razorpay")
public class PaymentResource {

    private final RazorpayService razorpayService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a Razorpay order for subscription payment")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            int recruiterId = ((Number) body.get("recruiterId")).intValue();
            String plan = (String) body.get("plan");
            double amount = ((Number) body.get("amount")).doubleValue();

            Map<String, Object> order = razorpayService.createOrder(recruiterId, plan, amount);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create payment order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment and activate subscription")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
        try {
            String orderId = (String) body.get("razorpay_order_id");
            String paymentId = (String) body.get("razorpay_payment_id");
            String signature = (String) body.get("razorpay_signature");
            int recruiterId = ((Number) body.get("recruiterId")).intValue();
            String plan = (String) body.get("plan");
            double amount = ((Number) body.get("amount")).doubleValue();

            boolean verified = razorpayService.verifyPayment(orderId, paymentId, signature);
            if (!verified) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Payment verification failed",
                        "verified", false));
            }

            // Payment verified — activate subscription with Razorpay paymentId as txn reference
            var subscription = subscriptionService.subscribe(recruiterId, plan, "RAZORPAY", amount);

            log.info("Payment verified and subscription activated: orderId={}, paymentId={}, recruiterId={}",
                    orderId, paymentId, recruiterId);

            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "paymentId", paymentId,
                    "subscription", subscription));
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Payment verification failed: " + e.getMessage()));
        }
    }

    @GetMapping("/key")
    @Operation(summary = "Get Razorpay public key for frontend checkout")
    public ResponseEntity<Map<String, String>> getKey() {
        return ResponseEntity.ok(Map.of("keyId", razorpayService.getKeyId()));
    }
}
