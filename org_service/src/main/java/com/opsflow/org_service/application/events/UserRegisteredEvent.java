package com.opsflow.org_service.application.events;

import java.io.Serializable;

public record UserRegisteredEvent(
    Long id,
    String username,
    String email,
    Long organizationId
) implements Serializable {}
