package com.hireconnect.profile.repository;

import com.hireconnect.profile.pojo.ParsedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParsedResumeRepository extends JpaRepository<ParsedResume, Integer> {

    Optional<ParsedResume> findByCandidateProfileId(int candidateProfileId);

    boolean existsByCandidateProfileId(int candidateProfileId);

    void deleteByCandidateProfileId(int candidateProfileId);
}
