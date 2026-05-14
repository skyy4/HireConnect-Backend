package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks individual job views per user for analytics.
 * Each record represents one view event — used by Analytics-Service
 * to compute view-to-apply ratios and job popularity metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_views", indexes = {
        @Index(name = "idx_jv_job", columnList = "job_id"),
        @Index(name = "idx_jv_viewer", columnList = "viewer_id")
})
public class JobView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int viewId;

    @Column(nullable = false, name = "job_id")
    private int jobId;

    /**
     * userId of the viewer. Can be 0 / null for anonymous/guest views.
     */
    @Column(name = "viewer_id")
    private Integer viewerId;

    /** IP address of the viewer for deduplication (optional) */
    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime viewedAt = LocalDateTime.now();

    /** Source of the view: SEARCH | DIRECT | RECOMMENDATION | EMAIL */
    @Column(length = 30)
    @Builder.Default
    private String source = "DIRECT";
}
