package com.opsflow.auth_service.application.dtos.request;

import jakarta.validation.constraints.*;
import java.util.Set;

public record SignupRequest(
    @NotBlank @Size(min = 3, max = 20) String username,
    @NotBlank @Size(max = 50) @Email String email,
    @NotBlank @Size(min = 6, max = 40) String password,
    @NotBlank @Size(min = 2, max = 50) String name,
    @NotBlank @Size(min = 2, max = 50) String lastname,
    Set<String> roles,
    Long organizationId
) {}
