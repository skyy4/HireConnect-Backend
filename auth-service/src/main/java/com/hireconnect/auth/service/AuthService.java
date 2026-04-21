package com.hireconnect.auth.service;

import com.hireconnect.auth.pojo.AuthResponse;
import com.hireconnect.auth.pojo.LoginRequest;
import com.hireconnect.auth.pojo.RegisterRequest;
import com.hireconnect.auth.pojo.UserCredential;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse oAuthLogin(String email, String role);

    void logout(String token);

    boolean validateToken(String token);

    AuthResponse refreshToken(String refreshToken);

    UserCredential getByEmail(String email);

    UserCredential getByUserId(int userId);

    void deleteByUserId(int userId);

    java.util.List<UserCredential> getAllUsers();

    UserCredential updateUser(UserCredential user);
}
