package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUserId(int userId);

    boolean existsByUserId(int userId);

    Optional<Wallet> findByUserIdAndStatus(int userId, String status);
}
