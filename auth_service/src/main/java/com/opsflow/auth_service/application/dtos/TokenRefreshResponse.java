package com.opsflow.auth_service.application.dtos;

import static com.opsflow.auth_service.domain.constants.AuthConstants.BEARER;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {
    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, BEARER);
    }
}
