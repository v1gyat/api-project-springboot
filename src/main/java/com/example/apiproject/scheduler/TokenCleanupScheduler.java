package com.example.apiproject.scheduler;

import com.example.apiproject.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupScheduler {

    private final RefreshTokenRepository repo;

    public TokenCleanupScheduler(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 AM every day
    public void purgeExpiredTokens() {
        repo.deleteExpiredTokens(LocalDateTime.now());
    }
}
