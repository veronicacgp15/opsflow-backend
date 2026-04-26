package com.opsflow.auth_service.application.services.port;

import com.opsflow.auth_service.infrastructure.entities.RefreshToken;

import java.util.Optional;

public interface RefreshTokenPort
{
    RefreshToken createRefreshToken(Long userId);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUserId(Long userId);
}
