package com.hireconnect.profile.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores structured data extracted from a candidate's uploaded resume PDF.
 * Created/updated whenever a candidate uploads or changes their resume.
 * Used by analytics, job matching, and candidate shortlisting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parsed_resumes")
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int resumeId;

    /** FK → candidate_profiles.profileId */
    @Column(nullable = false, unique = true)
    private int candidateProfileId;

    /** Public URL of the uploaded PDF (AWS S3 / local) */
    @Column(length = 500)
    private String resumeUrl;

    // ── Extracted sections ────────────────────────────────────────────────

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parsed_resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "skill")
    @Builder.Default
    private List<String> extractedSkills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parsed_resume_education", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "education_entry")
    @Builder.Default
    private List<String> educationEntries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parsed_resume_experience", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "experience_entry")
    @Builder.Default
    private List<String> experienceEntries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parsed_resume_certifications", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "certification")
    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    /** Extracted candidate name from resume */
    @Column(length = 100)
    private String extractedName;

    /** Extracted email from resume */
    @Column(length = 150)
    private String extractedEmail;

    /** Extracted phone number from resume */
    @Column(length = 20)
    private String extractedPhone;

    /** Summary/objective paragraph extracted from resume */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** Total years of experience inferred from work history */
    private int inferredExperienceYears;

    /** Raw text extracted from the PDF (for keyword search / AI processing) */
    @Column(columnDefinition = "LONGTEXT")
    private String rawText;

    /** Parsing status: PENDING | COMPLETED | FAILED */
    @Column(length = 20)
    @Builder.Default
    private String parseStatus = "PENDING";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime parsedAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
