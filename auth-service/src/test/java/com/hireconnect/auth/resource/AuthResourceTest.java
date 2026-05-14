package com.hireconnect.auth.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.AuthResponse;
import com.hireconnect.auth.pojo.LoginRequest;
import com.hireconnect.auth.pojo.RegisterRequest;
import com.hireconnect.auth.pojo.UserCredential;
import com.hireconnect.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Claims;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
@DisplayName("AuthResource Controller Tests")
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    // ── POST /api/v1/auth/register ────────────────────────────────────────

    @Test
    @DisplayName("POST /register — returns 201 with token on success")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@test.com");
        req.setPassword("pass123");
        req.setRole("CANDIDATE");

        AuthResponse resp = new AuthResponse();
        resp.setEmail("alice@test.com");
        resp.setToken("jwt-token");
        resp.setUserId(1);

        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("alice@test.com")))
                .andExpect(jsonPath("$.token", is("jwt-token")));
    }

    // ── POST /api/v1/auth/login ───────────────────────────────────────────

    @Test
    @DisplayName("POST /login — returns 200 with token on success")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("bob@test.com");
        req.setPassword("secret");

        AuthResponse resp = new AuthResponse();
        resp.setEmail("bob@test.com");
        resp.setToken("access-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("access-token")));
    }

    @Test
    @DisplayName("POST /login — returns 400 when service throws IllegalArgumentException")
    void login_invalidCredentials_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("bad@test.com");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    // ── POST /api/v1/auth/logout ──────────────────────────────────────────

    @Test
    @DisplayName("POST /logout — returns 200 with success message")
    void logout_validToken_returns200() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")));
    }

    // ── GET /api/v1/auth/validate ─────────────────────────────────────────

    @Test
    @DisplayName("GET /validate — returns 200 with valid=true when token is valid")
    void validateToken_valid_returns200() throws Exception {
        when(authService.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.extractEmail("good-token")).thenReturn("alice@test.com");
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.get("role")).thenReturn("CANDIDATE");
        when(mockClaims.get("userId")).thenReturn(1);
        when(jwtUtil.extractClaims("good-token")).thenReturn(mockClaims);

        mockMvc.perform(get("/api/v1/auth/validate").param("token", "good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.email", is("alice@test.com")));
    }

    @Test
    @DisplayName("GET /validate — returns 401 when token is invalid")
    void validateToken_invalid_returns401() throws Exception {
        when(authService.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/validate").param("token", "bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid", is(false)));
    }

    // ── GET /api/v1/auth/user/{userId} ────────────────────────────────────

    @Test
    @DisplayName("GET /user/{userId} — returns user without passwordHash")
    void getByUserId_found_returns200() throws Exception {
        UserCredential user = new UserCredential();
        user.setUserId(5);
        user.setEmail("user@test.com");
        user.setPasswordHash("should-be-hidden");

        when(authService.getByUserId(5)).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(5)))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ── DELETE /api/v1/auth/user/{userId} ─────────────────────────────────

    @Test
    @DisplayName("DELETE /user/{userId} — deletes user and returns message")
    void deleteUser_returns200WithMessage() throws Exception {
        doNothing().when(authService).deleteByUserId(10);

        mockMvc.perform(delete("/api/v1/auth/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));
    }
}
