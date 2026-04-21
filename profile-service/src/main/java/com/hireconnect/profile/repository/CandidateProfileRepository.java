package com.hireconnect.profile.repository;

import com.hireconnect.profile.pojo.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Integer> {

    Optional<CandidateProfile> findByEmail(String email);

    Optional<CandidateProfile> findByMobile(String mobile);

    Optional<CandidateProfile> findByUserId(int userId);

    List<CandidateProfile> findAllBySkillsContaining(String skill);

    boolean existsByUserId(int userId);
}
