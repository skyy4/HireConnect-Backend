package com.hireconnect.auth.repository;

import com.hireconnect.auth.pojo.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<UserCredential, Integer> {

    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findByUserId(int userId);

    boolean existsByEmail(String email);

    void deleteByUserId(int userId);
}
