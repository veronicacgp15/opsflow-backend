package com.opsflow.auth_service.application.dtos;


import java.util.List;

import static com.opsflow.auth_service.domain.constants.AuthConstants.BEARER;

public record JwtResponse(
    String token,
    String refreshToken,
    Long id,
    String username,
    String email,
    List<String> roles,
    String type
) {

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email, List<String> roles) {
        this(accessToken, refreshToken, id, username, email, roles, BEARER);
    }
}
