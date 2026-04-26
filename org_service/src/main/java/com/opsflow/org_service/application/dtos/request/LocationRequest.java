package com.opsflow.org_service.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationRequest(
    @NotBlank(message = "El nombre de la sede es obligatorio")
    String name,
    
    String address,
    
    String city,
    
    @NotNull(message = "El ID de la organización es obligatorio")
    Long organizationId
) {}
