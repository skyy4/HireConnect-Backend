package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    List<Subscription> findByRecruiterId(int recruiterId);

    List<Subscription> findByStatus(String status);

    Optional<Subscription> findFirstByRecruiterIdAndStatusOrderByCreatedAtDesc(int recruiterId, String status);

    long countByPlan(String plan);

    boolean existsByRecruiterIdAndStatus(int recruiterId, String status);
}
