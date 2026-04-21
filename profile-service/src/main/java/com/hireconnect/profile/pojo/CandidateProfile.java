package com.hireconnect.profile.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int profileId;

    @Column(nullable = false, unique = true)
    private int userId; // FK → auth-service

    @NotBlank
    @Column(nullable = false, length = 100)
    private String fullName;

    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 15)
    private String mobile;

    private LocalDate dob;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    @Builder.Default
    private int experienceYears = 0;

    @Column(length = 100)
    private String currentJobTitle;

    @Column(length = 100)
    private String currentCompany;

    @Column(length = 100)
    private String highestEducation;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String preferredJobType = "FULL_TIME"; // FULL_TIME | PART_TIME | INTERNSHIP | FREELANCE

    private Double expectedSalary;

    @Column(length = 100)
    private String preferredLocation;

    @Column(length = 500)
    private String resumeUrl;

    @Column(length = 500)
    private String profilePictureUrl;

    @Column(length = 500)
    private String linkedinUrl;

    @Column(length = 500)
    private String githubUrl;

    @Column(length = 500)
    private String portfolioUrl;

    // Comma-separated skills stored as a list
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "houseNo",  column = @Column(name = "house_no")),
        @AttributeOverride(name = "street",   column = @Column(name = "street")),
        @AttributeOverride(name = "city",     column = @Column(name = "city")),
        @AttributeOverride(name = "state",    column = @Column(name = "state")),
        @AttributeOverride(name = "pincode",  column = @Column(name = "pincode")),
        @AttributeOverride(name = "country",  column = @Column(name = "country"))
    })
    private Address address;
}
