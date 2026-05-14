package com.hireconnect.auth.resource;

import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.*;
import com.hireconnect.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Service", description = "Registration, Login, JWT, OAuth2")
public class AuthResource {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Candidate or Recruiter)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email={} role={}", request.getEmail(), request.getRole());
        AuthResponse response = authService.register(request);
        log.info("User registered successfully: userId={} email={}", response.getUserId(), response.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Login successful for email={}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate current session token")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info("Logout request received");
        authService.logout(token);
        log.debug("Token invalidated successfully");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        log.debug("Token refresh request received");
        String refreshToken = body.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate a JWT token (used by API Gateway)")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam("token") String token) {
        boolean valid = authService.validateToken(token);
        if (!valid) {
            log.warn("Token validation failed — token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token is invalid or expired"));
        }
        String email = jwtUtil.extractEmail(token);
        var claims = jwtUtil.extractClaims(token);
        log.debug("Token validated for email={}", email);
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "email", email,
                "role", claims.get("role"),
                "userId", claims.get("userId")
        ));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user credential by userId")
    public ResponseEntity<UserCredential> getByUserId(@PathVariable int userId) {
        log.debug("Fetching user credential for userId={}", userId);
        UserCredential credential = authService.getByUserId(userId);
        credential.setPasswordHash(null); // never expose hash
        return ResponseEntity.ok(credential);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete user credentials (Admin only)")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable int userId) {
        log.warn("Delete user request for userId={}", userId);
        authService.deleteByUserId(userId);
        log.info("User deleted: userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<java.util.List<UserCredential>> getAllUsers() {
        log.debug("Fetching all users");
        java.util.List<UserCredential> users = authService.getAllUsers();
        users.forEach(u -> u.setPasswordHash(null));
        log.debug("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user/{userId}/toggle-status")
    @Operation(summary = "Toggle user active status (Admin only)")
    public ResponseEntity<Map<String, String>> toggleUser(@PathVariable int userId) {
        log.info("Toggle status for userId={}", userId);
        return applyUserStatus(userId, null);
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user active status (Admin only)")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable int userId,
            @RequestBody(required = false) Map<String, Object> body) {
        Boolean active = body != null && body.get("active") != null
                ? Boolean.valueOf(String.valueOf(body.get("active")))
                : null;
        log.info("Update user status: userId={} active={}", userId, active);
        return applyUserStatus(userId, active);
    }

    private ResponseEntity<Map<String, String>> applyUserStatus(int userId, Boolean active) {
        UserCredential user = authService.getByUserId(userId);
        user.setActive(active != null ? active : !user.isActive());
        authService.updateUser(user);
        log.info("User status updated: userId={} active={}", userId, user.isActive());
        return ResponseEntity.ok(Map.of(
                "message", "User status updated",
                "active", String.valueOf(user.isActive())));
    }
}
