package com.hireconnect.auth.resource;

import com.hireconnect.auth.config.JwtUtil;
import com.hireconnect.auth.pojo.*;
import com.hireconnect.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate current session token")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token is invalid or expired"));
        }
        String email = jwtUtil.extractEmail(token);
        var claims = jwtUtil.extractClaims(token);
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
        UserCredential credential = authService.getByUserId(userId);
        credential.setPasswordHash(null); // never expose hash
        return ResponseEntity.ok(credential);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete user credentials (Admin only)")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable int userId) {
        authService.deleteByUserId(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<java.util.List<UserCredential>> getAllUsers() {
        java.util.List<UserCredential> users = authService.getAllUsers();
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user/{userId}/toggle-status")
    @Operation(summary = "Toggle user active status (Admin only)")
    public ResponseEntity<Map<String, String>> toggleUser(@PathVariable int userId) {
        UserCredential user = authService.getByUserId(userId);
        user.setActive(!user.isActive());
        authService.updateUser(user);
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }
}
