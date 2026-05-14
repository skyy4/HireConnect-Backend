package com.hireconnect.job.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs")
public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int jobId;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 100)
    private String category; // IT, Finance, Marketing, etc.

    @Column(length = 50)
    private String type; // FULL_TIME | PART_TIME | INTERNSHIP | CONTRACT | REMOTE

    @Column(length = 120)
    private String location;

    private Double salaryMin;

    private Double salaryMax;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skills;

    private int experienceRequired; // years

    @Column(nullable = false)
    private int postedBy; // recruiterId (userId from auth)

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE | PAUSED | CLOSED

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime postedAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(length = 50)
    private String experienceLevel; // ENTRY | MID | SENIOR | EXECUTIVE

    @Column(length = 300)
    private String companyName;

    @Column(length = 500)
    private String companyLogoUrl;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
