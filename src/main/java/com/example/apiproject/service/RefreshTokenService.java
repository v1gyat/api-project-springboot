package com.example.apiproject.service;

import com.example.apiproject.entity.RefreshToken;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.InvalidTokenException;
import com.example.apiproject.exception.TokenReusedException;
import com.example.apiproject.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service @Transactional
public class RefreshTokenService {

    private static final long REFRESH_EXPIRY_DAYS = 7L;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    // -- ISSUE -----------------------------------------------------------------
    public String createRefreshToken(User user, String familyId) {
        String rawToken = UUID.randomUUID().toString()   // opaque random value
                        + UUID.randomUUID().toString();  // extra entropy
        String hash = sha256(rawToken);

        RefreshToken entity = RefreshToken.builder()
            .user(user)
            .tokenHash(hash)
            .familyId(familyId != null ? familyId : UUID.randomUUID().toString())
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(REFRESH_EXPIRY_DAYS))
            .build();
        repo.save(entity);

        return rawToken; // send this to client, NEVER persist it
    }

    // -- ROTATE ----------------------------------------------------------------
    public RefreshToken verifyAndConsume(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken stored = repo.findByTokenHash(hash)
            .orElseThrow(() -> new InvalidTokenException("Unknown refresh token"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new InvalidTokenException("Token expired or revoked");
        }

        // !! REUSE DETECTION - token already replaced = stolen token
        if (stored.isReplaced()) {
            revokeFamily(stored.getFamilyId());  // nuke entire chain
            throw new TokenReusedException("Refresh token reuse detected - all sessions revoked");
        }

        return stored; // caller will now create new token and link it
    }

    public void markReplaced(RefreshToken old, String newTokenId) {
        old.setReplacedBy(newTokenId);
        repo.save(old);
    }

    // -- REVOKE ----------------------------------------------------------------
    public void revokeToken(String rawToken) {
        sha256lookup(rawToken).ifPresent(t -> {
            t.setRevokedAt(LocalDateTime.now());
            repo.save(t);
        });
    }

    private Optional<RefreshToken> sha256lookup(String rawToken) {
        try {
            return repo.findByTokenHash(sha256(rawToken));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void revokeFamily(String familyId) {
        repo.findByFamilyId(familyId).forEach(t -> {
            if (!t.isRevoked()) t.setRevokedAt(LocalDateTime.now());
        });
        repo.flush();
    }

    public void revokeAllForUser(User user) {
        repo.findByUserAndRevokedAtIsNull(user).forEach(t ->
            t.setRevokedAt(LocalDateTime.now()));
        repo.flush();
    }

    // -- HELPERS ---------------------------------------------------------------
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
