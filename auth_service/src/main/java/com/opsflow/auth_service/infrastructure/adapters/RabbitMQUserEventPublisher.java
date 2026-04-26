package com.opsflow.auth_service.infrastructure.adapters;

import com.opsflow.auth_service.application.events.UserRegisteredEvent;
import com.opsflow.auth_service.application.ports.UserEventPublisher;
import com.opsflow.auth_service.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.opsflow.auth_service.domain.constants.AuthConstants.*;

@Component
public class RabbitMQUserEventPublisher implements UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQUserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
            AUTH_EXCHANGE,
            USER_REGISTERED_ROUTING_KEY,
            event
        );
    }
}
