package com.opsflow.auth_service.application.dtos.request;

public record TokenRefreshRequest(String accessToken, String refreshToken) {}
