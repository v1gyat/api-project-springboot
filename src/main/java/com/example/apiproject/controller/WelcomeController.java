package com.example.apiproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        return Map.of(
                "message", "Welcome to Spring Boot API Project!",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "health", "/api/health",
                        "documentation", "Coming soon..."));
    }

}
