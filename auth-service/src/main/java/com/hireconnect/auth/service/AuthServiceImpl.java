package com.hireconnect.auth.service;

import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.*;
import com.hireconnect.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String BLACKLIST_PREFIX = "blacklisted_token:";

    private final AuthRepository authRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

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
        try {
            // Calculate remaining TTL from token expiration
            long expirationMs = jwtUtil.extractClaims(token).getExpiration().getTime();
            long remainingMs = expirationMs - System.currentTimeMillis();
            if (remainingMs > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token, "LOGGED_OUT",
                        Duration.ofMillis(remainingMs));
                log.info("Token blacklisted with TTL {}ms", remainingMs);
            }
        } catch (Exception ex) {
            log.warn("Could not blacklist token via Redis (falling back to client-side): {}", ex.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            return false;
        }
        // Check Redis blacklist
        try {
            Boolean isBlacklisted = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.debug("Token is blacklisted");
                return false;
            }
        } catch (Exception ex) {
            log.warn("Redis unavailable for blacklist check, skipping: {}", ex.getMessage());
        }
        return true;
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
