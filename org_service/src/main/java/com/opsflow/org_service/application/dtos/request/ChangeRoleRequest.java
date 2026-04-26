package com.opsflow.org_service.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeRoleRequest(
    @NotBlank String roleName
) {}
