package com.hireconnect.job.repository;

import com.hireconnect.job.entity.JobView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobViewRepository extends JpaRepository<JobView, Integer> {

    long countByJobId(int jobId);

    long countByViewerId(int viewerId);

    List<JobView> findByJobId(int jobId);

    List<JobView> findByViewerId(int viewerId);

    @Query("SELECT v FROM JobView v WHERE v.jobId = :jobId AND v.viewedAt BETWEEN :from AND :to")
    List<JobView> findByJobIdAndDateRange(
            @Param("jobId") int jobId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT v.jobId, COUNT(v) AS total FROM JobView v GROUP BY v.jobId ORDER BY total DESC")
    List<Object[]> findTopViewedJobs();

    boolean existsByJobIdAndViewerIdAndViewedAtAfter(int jobId, int viewerId, LocalDateTime after);
}
