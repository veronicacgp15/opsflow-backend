package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.infrastructure.entities.RefreshToken;
import com.opsflow.auth_service.infrastructure.repositories.RefreshTokenRepository;
import com.opsflow.auth_service.infrastructure.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceCOPIA {

    @Value("${app.jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceCOPIA(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findById(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.findByUsername(username).ifPresent(refreshTokenRepository::delete);

        var refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setId(UUID.randomUUID().toString());
        refreshToken.setTtl(refreshTokenDurationMs / 1000);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUsername(String username) {
        refreshTokenRepository.findByUsername(username).ifPresent(refreshTokenRepository::delete);
    }
}
