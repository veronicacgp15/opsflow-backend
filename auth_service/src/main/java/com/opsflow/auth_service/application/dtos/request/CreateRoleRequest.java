package com.opsflow.auth_service.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
    @NotBlank @Size(min = 3, max = 50)
    String name
) {}
