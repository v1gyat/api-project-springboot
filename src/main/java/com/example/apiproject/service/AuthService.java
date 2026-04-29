package com.example.apiproject.service;

import com.example.apiproject.dto.AuthResponse;
import com.example.apiproject.dto.LoginRequest;
import com.example.apiproject.dto.RegisterRequest;
import com.example.apiproject.entity.RefreshToken;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.EmailAlreadyExistsException;
import com.example.apiproject.repository.RefreshTokenRepository;
import com.example.apiproject.repository.UserRepository;
import com.example.apiproject.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new EmailAlreadyExistsException("Email already in use");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        String accessToken = jwtUtils.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user, null);

        return new AuthResponse(accessToken, refreshToken, "Login successful");
    }

    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken old = refreshTokenService.verifyAndConsume(rawRefreshToken);
        User user = old.getUser();

        String newAccessToken = jwtUtils.generateToken(user.getEmail());
        String newRawRefresh = refreshTokenService.createRefreshToken(user, old.getFamilyId());

        RefreshToken newStored = refreshTokenRepository.findByTokenHash(
            RefreshTokenService.sha256(newRawRefresh)).orElseThrow();
        refreshTokenService.markReplaced(old, newStored.getId());

        return new AuthResponse(newAccessToken, newRawRefresh, "Token refreshed successfully");
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    public void logoutAll(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        refreshTokenService.revokeAllForUser(user);
    }
}
