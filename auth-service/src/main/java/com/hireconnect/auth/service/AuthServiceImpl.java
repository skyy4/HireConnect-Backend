package com.hireconnect.auth.service;

import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.*;
import com.hireconnect.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long tokenExpiry;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        UserCredential credential = UserCredential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider("LOCAL")
                .build();
        UserCredential saved = authRepository.save(credential);
        return buildAuthResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserCredential credential = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!credential.isActive()) {
            throw new IllegalStateException("Account is suspended. Contact support.");
        }
        return buildAuthResponse(credential);
    }

    @Override
    @Transactional
    public AuthResponse oAuthLogin(String email, String role) {
        UserCredential credential = authRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserCredential newUser = UserCredential.builder()
                            .email(email)
                            .passwordHash("")
                            .role(role != null ? role : "CANDIDATE")
                            .provider("GITHUB")
                            .active(true)
                            .build();
                    return authRepository.save(newUser);
                });
        return buildAuthResponse(credential);
    }

    @Override
    public void logout(String token) {
        // In a production system, add token to a Redis blacklist
        // For now, client-side token removal handles logout
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        UserCredential credential = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return buildAuthResponse(credential);
    }

    @Override
    public UserCredential getByEmail(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    public UserCredential getByUserId(int userId) {
        return authRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public void deleteByUserId(int userId) {
        authRepository.deleteByUserId(userId);
    }

    @Override
    public java.util.List<UserCredential> getAllUsers() {
        return authRepository.findAll();
    }

    @Override
    @Transactional
    public UserCredential updateUser(UserCredential user) {
        return authRepository.save(user);
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(UserCredential credential) {
        String accessToken = jwtUtil.generateAccessToken(
                credential.getEmail(), credential.getRole(), credential.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(credential.getEmail());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .userId(credential.getUserId())
                .email(credential.getEmail())
                .role(credential.getRole())
                .build();
    }
}
