package com.opsflow.org_service.infrastructure.adapters.messaging;

import com.opsflow.org_service.domain.constants.OrgConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue authUserRegisteredQueue() {
        return new Queue(OrgConstants.AUTH_USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(OrgConstants.OPSFLOW_EXCHANGE);
    }

    @Bean
    public Binding bindingAuthUserRegistered(Queue authUserRegisteredQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authUserRegisteredQueue)
                .to(exchange)
                .with(OrgConstants.AUTH_USER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter consumerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
