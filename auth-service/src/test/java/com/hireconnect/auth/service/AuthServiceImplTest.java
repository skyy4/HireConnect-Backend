package com.hireconnect.auth.service;

import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.LoginRequest;
import com.hireconnect.auth.pojo.RegisterRequest;
import com.hireconnect.auth.pojo.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");
        req.setRole("CANDIDATE");

        when(authRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        
        UserCredential saved = new UserCredential();
        saved.setUserId(1);
        saved.setEmail("test@test.com");
        saved.setRole("CANDIDATE");
        when(authRepository.save(any(UserCredential.class))).thenReturn(saved);

        when(jwtUtil.generateAccessToken(any(), any(), anyInt())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        var res = authService.register(req);
        assertNotNull(res);
        assertEquals("test@test.com", res.getEmail());
        assertEquals("token", res.getToken());
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        UserCredential user = new UserCredential();
        user.setEmail("test@test.com");
        user.setPasswordHash("hashed");
        user.setActive(true);

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(), any(), anyInt())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");

        var res = authService.login(req);
        assertNotNull(res);
        assertEquals("token", res.getToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("wrong");

        UserCredential user = new UserCredential();
        user.setPasswordHash("hashed");

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }
}
