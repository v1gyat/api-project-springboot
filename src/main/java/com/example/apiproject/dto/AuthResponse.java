package com.example.apiproject.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String message
) {}
