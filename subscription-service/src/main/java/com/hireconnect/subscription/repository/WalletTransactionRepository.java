package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {

    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(int userId);

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(int walletId);

    List<WalletTransaction> findByUserIdAndType(int userId, String type);

    List<WalletTransaction> findByUserIdAndCreatedAtBetween(int userId, LocalDateTime from, LocalDateTime to);

    long countByUserIdAndStatus(int userId, String status);
}
