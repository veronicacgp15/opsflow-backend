package com.opsflow.document_service.infrastructure.adapters.messaging;

import com.opsflow.document_service.application.dtos.DocumentEvent;
import com.opsflow.document_service.domain.models.DocumentDomain;
import com.opsflow.document_service.domain.port.out.DocumentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisherAdapter implements DocumentEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:document-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.created:document.created}")
    private String documentCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.updated:document.updated}")
    private String documentUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.expiring:document.expiring}")
    private String documentExpiringRoutingKey;

    @Value("${app.rabbitmq.routing-key.expired:document.expired}")
    private String documentExpiredRoutingKey;

    @Override
    public void publishDocumentCreatedEvent(DocumentDomain document) {
        DocumentEvent event = mapToEvent(document, "CREATED");
        log.info("Enviando evento DOCUMENT_CREATED a RabbitMQ para el documento ID: {}", event.id());
        rabbitTemplate.convertAndSend(exchange, documentCreatedRoutingKey, event);
    }

    @Override
    public void publishDocumentUpdatedEvent(DocumentDomain document) {
        DocumentEvent event = mapToEvent(document, "UPDATED");
        log.info("Enviando evento DOCUMENT_UPDATED a RabbitMQ para el documento ID: {}", event.id());
        rabbitTemplate.convertAndSend(exchange, documentUpdatedRoutingKey, event);
    }

    @Override
    public void publishDocumentExpiringEvent(DocumentDomain document) {
        DocumentEvent event = mapToEvent(document, "EXPIRING");
        log.info("Enviando evento DOCUMENT_EXPIRING a RabbitMQ para el documento ID: {}", event.id());
        rabbitTemplate.convertAndSend(exchange, documentExpiringRoutingKey, event);
    }

    @Override
    public void publishDocumentExpiredEvent(DocumentDomain document) {
        DocumentEvent event = mapToEvent(document, "EXPIRED");
        log.info("Enviando evento DOCUMENT_EXPIRED a RabbitMQ para el documento ID: {}", event.id());
        rabbitTemplate.convertAndSend(exchange, documentExpiredRoutingKey, event);
    }

    private DocumentEvent mapToEvent(DocumentDomain doc, String type) {
        return new DocumentEvent(
                doc.getId(),
                doc.getName(),
                doc.getOrganizationId(),
                doc.getExpirationDate(),
                doc.getStatus() != null ? doc.getStatus().name() : null,
                type
        );
    }
}
