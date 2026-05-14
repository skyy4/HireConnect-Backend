package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {

    List<Bookmark> findByCandidateId(int candidateId);

    List<Bookmark> findByJobId(int jobId);

    Optional<Bookmark> findByCandidateIdAndJobId(int candidateId, int jobId);

    boolean existsByCandidateIdAndJobId(int candidateId, int jobId);

    void deleteByCandidateIdAndJobId(int candidateId, int jobId);

    long countByJobId(int jobId);

    long countByCandidateId(int candidateId);
}
