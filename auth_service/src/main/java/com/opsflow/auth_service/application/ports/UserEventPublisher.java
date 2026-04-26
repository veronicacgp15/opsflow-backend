package com.opsflow.auth_service.application.ports;

import com.opsflow.auth_service.application.events.UserRegisteredEvent;

public interface UserEventPublisher {
    void publishUserRegistered(UserRegisteredEvent event);
}
