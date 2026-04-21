package com.hireconnect.auth.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role; // CANDIDATE | RECRUITER | ADMIN

    @Column(length = 30)
    @Builder.Default
    private String provider = "LOCAL"; // LOCAL | GITHUB

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── constructors helper ──────────────────────────────────────────────────
    public UserCredential(String email, String passwordHash, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.provider = "LOCAL";
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}
