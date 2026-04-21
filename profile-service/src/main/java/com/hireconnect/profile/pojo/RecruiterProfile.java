package com.hireconnect.profile.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recruiter_profiles")
public class RecruiterProfile {

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

    @Column(length = 100)
    private String designation;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(length = 50)
    private String companySize; // e.g. "1-10", "11-50", "51-200"

    @Column(length = 100)
    private String industry;

    @Column(length = 300)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    @Column(length = 500)
    private String companyLogoUrl;

    @Column(length = 100)
    private String location;

    @Column(length = 500)
    private String linkedinUrl;

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
