package com.hireconnect.subscription.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RazorpayService {

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(keyId, keySecret);
            log.info("Razorpay client initialized with key: {}...", keyId.substring(0, Math.min(12, keyId.length())));
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
        }
    }

    /**
     * Creates a Razorpay order. Amount is in INR (rupees) — we convert to paise internally.
     */
    public Map<String, Object> createOrder(int recruiterId, String plan, double amountInRupees) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amountInRupees * 100)); // Convert to paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "hc_" + plan.toLowerCase() + "_" + recruiterId + "_" + System.currentTimeMillis());
        orderRequest.put("notes", new JSONObject()
                .put("recruiterId", String.valueOf(recruiterId))
                .put("plan", plan));

        Order order = razorpayClient.orders.create(orderRequest);
        String createdOrderId = order.get("id");
        log.info("Razorpay order created: {}", createdOrderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("receipt", order.get("receipt"));
        response.put("status", order.get("status"));
        response.put("keyId", keyId);
        return response;
    }

    /**
     * Verifies Razorpay payment signature to ensure it's authentic.
     */
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException e) {
            log.error("Payment verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String getKeyId() {
        return keyId;
    }
}
