package com.example.apiproject.controller;

import com.example.apiproject.dto.AuthResponse;
import com.example.apiproject.dto.LoginRequest;
import com.example.apiproject.dto.RegisterRequest;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.repository.UserRepository;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Register a new user (ADMIN ONLY)
     * POST /api/auth/register
     * Only ADMIN can create new users
     * ADMIN can specify role (USER, MANAGER, or ADMIN)
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Admin requested to register new user: {}", request.getEmail());

        // 1. Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Email already in use",
                    null,
                    null,
                    LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. Create new user with role specified by ADMIN (defaults to USER if not
        // provided)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password set by ADMIN

        // Set role: use provided role if present, otherwise default to USER
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        // 3. Save to database
        userRepository.save(user);

        // 4. Return success response
        ApiResponse<String> response = new ApiResponse<>(
                true,
                "User registered successfully",
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login and get JWT token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        // 1. Authenticate user using AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // 2. If authentication successful, generate JWT token using the authenticated
        // user's email
        String token = jwtUtils.generateToken(authentication.getName());

        // 3. Create AuthResponse
        AuthResponse authResponse = new AuthResponse(token, "Login successful");

        // 4. Wrap in ApiResponse and return
        ApiResponse<AuthResponse> response = new ApiResponse<>(
                true,
                "Authentication successful",
                authResponse,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
