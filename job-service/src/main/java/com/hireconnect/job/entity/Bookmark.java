package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a saved/bookmarked job for a candidate.
 * A candidate can bookmark any number of ACTIVE jobs for later review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookmarks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookmarkId;

    /** userId of the candidate (from auth-service) */
    @Column(nullable = false, name = "candidate_id")
    private int candidateId;

    /** FK → jobs.jobId */
    @Column(nullable = false, name = "job_id")
    private int jobId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime savedAt = LocalDateTime.now();

    @Column(length = 500)
    private String note; // Optional personal note added by the candidate
}
