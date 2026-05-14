package com.hireconnect.subscription.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.Wallet;
import com.hireconnect.subscription.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionResource.class)
@DisplayName("SubscriptionResource Controller Tests")
class SubscriptionResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private Subscription buildSubscription(int id, String plan, String status) {
        Subscription s = new Subscription();
        s.setSubscriptionId(id);
        s.setRecruiterId(10);
        s.setPlan(plan);
        s.setStatus(status);
        s.setAmountPaid(999.0);
        return s;
    }

    // ── POST /api/v1/subscriptions ─────────────────────────────────────────

    @Test
    @DisplayName("POST /subscriptions — creates subscription and returns 201")
    void subscribe_validRequest_returns201() throws Exception {
        Subscription saved = buildSubscription(1, "PROFESSIONAL", "ACTIVE");
        when(subscriptionService.subscribe(10, "PROFESSIONAL", "CARD", 999.0)).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "recruiterId", 10,
                "plan", "PROFESSIONAL",
                "paymentMode", "CARD",
                "amount", 999.0
        );

        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plan", is("PROFESSIONAL")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    // ── GET /api/v1/subscriptions/recruiter/{id}/active ───────────────────

    @Test
    @DisplayName("GET /subscriptions/recruiter/{id}/active — returns 200 with active plan")
    void getActive_found_returns200() throws Exception {
        Subscription active = buildSubscription(2, "ENTERPRISE", "ACTIVE");
        when(subscriptionService.getActiveSubscription(10)).thenReturn(active);

        mockMvc.perform(get("/api/v1/subscriptions/recruiter/10/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan", is("ENTERPRISE")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("GET /subscriptions/recruiter/{id}/active — returns 204 when no active subscription")
    void getActive_notFound_returns204() throws Exception {
        when(subscriptionService.getActiveSubscription(10))
                .thenThrow(new IllegalStateException("No active subscription"));

        mockMvc.perform(get("/api/v1/subscriptions/recruiter/10/active"))
                .andExpect(status().isNoContent());
    }

    // ── GET /api/v1/subscriptions/recruiter/{id} ──────────────────────────

    @Test
    @DisplayName("GET /subscriptions/recruiter/{id} — returns all subscriptions for recruiter")
    void getAll_returns200() throws Exception {
        List<Subscription> list = List.of(
                buildSubscription(1, "FREE", "CANCELLED"),
                buildSubscription(2, "PROFESSIONAL", "ACTIVE")
        );
        when(subscriptionService.getByRecruiter(10)).thenReturn(list);

        mockMvc.perform(get("/api/v1/subscriptions/recruiter/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ── PATCH /api/v1/subscriptions/recruiter/{id}/cancel ─────────────────

    @Test
    @DisplayName("PATCH /subscriptions/recruiter/{id}/cancel — cancels subscription")
    void cancel_returns200() throws Exception {
        Subscription cancelled = buildSubscription(1, "PROFESSIONAL", "CANCELLED");
        when(subscriptionService.cancelSubscription(10)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/v1/subscriptions/recruiter/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    // ── GET /api/v1/wallet/{userId} ───────────────────────────────────────

    @Test
    @DisplayName("GET /wallet/{userId} — returns wallet details")
    void getWallet_returns200() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setWalletId(1);
        wallet.setUserId(5);
        wallet.setBalance(500.0);
        wallet.setStatus("ACTIVE");

        when(subscriptionService.getWallet(5)).thenReturn(wallet);

        mockMvc.perform(get("/api/v1/wallet/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(500.0)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    // ── GET /api/v1/wallet/{userId}/balance ───────────────────────────────

    @Test
    @DisplayName("GET /wallet/{userId}/balance — returns current balance")
    void getBalance_returns200() throws Exception {
        when(subscriptionService.getWalletBalance(5)).thenReturn(750.0);

        mockMvc.perform(get("/api/v1/wallet/5/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(750.0)));
    }
}
