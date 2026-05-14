package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.Wallet;
import com.hireconnect.subscription.entity.WalletTransaction;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import com.hireconnect.subscription.repository.WalletRepository;
import com.hireconnect.subscription.repository.WalletTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl Unit Tests")
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Subscription buildSubscription(int id, String plan, String status) {
        return Subscription.builder()
                .subscriptionId(id)
                .recruiterId(1)
                .plan(plan)
                .status(status)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .amountPaid(999.0)
                .build();
    }

    private Wallet buildWallet(int userId, double balance) {
        return Wallet.builder()
                .walletId(1)
                .userId(userId)
                .balance(balance)
                .currency("INR")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Subscription Tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("subscribe — creates ACTIVE subscription and generates invoice")
    void subscribe_createsActiveSubscription() {
        Subscription saved = buildSubscription(1, "PROFESSIONAL", "ACTIVE");
        Invoice invoice = Invoice.builder().subscriptionId(1).amount(999.0).status("PAID").build();

        when(subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(1, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenReturn(saved);
        when(invoiceRepository.save(any())).thenReturn(invoice);

        Subscription result = subscriptionService.subscribe(1, "PROFESSIONAL", "RAZORPAY", 999.0);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getPlan()).isEqualTo("PROFESSIONAL");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("subscribe — cancels existing ACTIVE subscription before creating new one")
    void subscribe_cancelsExistingFirst() {
        Subscription existing = buildSubscription(1, "PROFESSIONAL", "ACTIVE");
        Subscription newSub = buildSubscription(2, "ENTERPRISE", "ACTIVE");
        Invoice invoice = Invoice.builder().build();

        when(subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(1, "ACTIVE"))
                .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any())).thenReturn(newSub);
        when(invoiceRepository.save(any())).thenReturn(invoice);

        subscriptionService.subscribe(1, "ENTERPRISE", "WALLET", 4999.0);

        verify(subscriptionRepository, atLeast(2)).save(any());
    }

    @Test
    @DisplayName("getActiveSubscription — returns active subscription")
    void getActiveSubscription_returnsSubscription() {
        Subscription sub = buildSubscription(1, "PROFESSIONAL", "ACTIVE");
        when(subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(1, "ACTIVE"))
                .thenReturn(Optional.of(sub));

        assertThat(subscriptionService.getActiveSubscription(1).getPlan()).isEqualTo("PROFESSIONAL");
    }

    @Test
    @DisplayName("getActiveSubscription — throws when no active subscription")
    void getActiveSubscription_noActive_throws() {
        when(subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(anyInt(), eq("ACTIVE")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getActiveSubscription(99))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cancelSubscription — sets status to CANCELLED")
    void cancelSubscription_setsStatusCancelled() {
        Subscription sub = buildSubscription(1, "PROFESSIONAL", "ACTIVE");
        Subscription cancelled = buildSubscription(1, "PROFESSIONAL", "CANCELLED");

        when(subscriptionRepository.findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(1, "ACTIVE"))
                .thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(cancelled);

        Subscription result = subscriptionService.cancelSubscription(1);
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    // ── Wallet Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createWallet — creates new wallet with zero balance")
    void createWallet_success() {
        Wallet wallet = buildWallet(5, 0.0);
        when(walletRepository.existsByUserId(5)).thenReturn(false);
        when(walletRepository.save(any())).thenReturn(wallet);

        Wallet result = subscriptionService.createWallet(5, "RECRUITER");
        assertThat(result.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("createWallet — throws when wallet already exists")
    void createWallet_duplicate_throws() {
        when(walletRepository.existsByUserId(5)).thenReturn(true);
        assertThatThrownBy(() -> subscriptionService.createWallet(5, "RECRUITER"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("creditWallet — increases balance and records transaction")
    void creditWallet_increasesBalance() {
        Wallet wallet = buildWallet(5, 100.0);
        when(walletRepository.findByUserId(5)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        when(walletTransactionRepository.save(any())).thenReturn(WalletTransaction.builder().build());

        Wallet result = subscriptionService.creditWallet(5, 500.0, "UPI", "TXN123", "Top up");
        assertThat(result.getBalance()).isEqualTo(600.0);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("debitWallet — decreases balance and records transaction")
    void debitWallet_decreasesBalance() {
        Wallet wallet = buildWallet(5, 1000.0);
        when(walletRepository.findByUserId(5)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        when(walletTransactionRepository.save(any())).thenReturn(WalletTransaction.builder().build());

        Wallet result = subscriptionService.debitWallet(5, 300.0, "WALLET", "Subscription");
        assertThat(result.getBalance()).isEqualTo(700.0);
    }

    @Test
    @DisplayName("debitWallet — throws on insufficient balance")
    void debitWallet_insufficientBalance_throws() {
        Wallet wallet = buildWallet(5, 50.0);
        when(walletRepository.findByUserId(5)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> subscriptionService.debitWallet(5, 500.0, "WALLET", "Subscription"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient");
    }

    @Test
    @DisplayName("creditWallet — throws on non-positive amount")
    void creditWallet_negativeAmount_throws() {
        // Amount check happens before repo call, so no stub needed
        assertThatThrownBy(() -> subscriptionService.creditWallet(5, -50.0, "UPI", "TXN", "desc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getWalletBalance — returns wallet balance")
    void getWalletBalance_returnsBalance() {
        Wallet wallet = buildWallet(5, 250.0);
        when(walletRepository.findByUserId(5)).thenReturn(Optional.of(wallet));
        assertThat(subscriptionService.getWalletBalance(5)).isEqualTo(250.0);
    }
}
