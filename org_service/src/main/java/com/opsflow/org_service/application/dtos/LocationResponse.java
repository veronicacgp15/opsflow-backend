package com.opsflow.org_service.application.dtos;

public record LocationResponse(
    Long id,
    String name,
    String address,
    String city,
    Long organizationId
) {}
