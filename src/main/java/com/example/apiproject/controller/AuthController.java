package com.example.apiproject.controller;

import com.example.apiproject.dto.AuthResponse;
import com.example.apiproject.dto.LoginRequest;
import com.example.apiproject.dto.RefreshRequest;
import com.example.apiproject.dto.RegisterRequest;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, "User registered successfully", null, null, LocalDateTime.now()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Authentication successful", authService.login(request), null, LocalDateTime.now()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Token refreshed", authService.refresh(request.getRefreshToken()), null, LocalDateTime.now()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out", null, null, LocalDateTime.now()));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logoutAll(Authentication auth) {
        authService.logoutAll(auth.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "All sessions revoked", null, null, LocalDateTime.now()));
    }
}
