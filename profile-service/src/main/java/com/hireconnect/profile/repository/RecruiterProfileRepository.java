package com.hireconnect.profile.repository;

import com.hireconnect.profile.pojo.RecruiterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Integer> {

    Optional<RecruiterProfile> findByEmail(String email);

    Optional<RecruiterProfile> findByMobile(String mobile);

    Optional<RecruiterProfile> findByUserId(int userId);

    boolean existsByUserId(int userId);
}
