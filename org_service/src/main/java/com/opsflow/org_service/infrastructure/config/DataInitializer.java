package com.opsflow.org_service.infrastructure.config;

import com.opsflow.org_service.application.dtos.request.OrganizationRequest;
import com.opsflow.org_service.domain.ports.in.OrganizationServicePort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final OrganizationServicePort organizationService;

    public DataInitializer(OrganizationServicePort organizationService) {
        this.organizationService = organizationService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (organizationService.findAll().isEmpty()) {
            OrganizationRequest defaultOrg = new OrganizationRequest(
                    "OpsFlow Central",
                    "TAX-123456789",
                    "123 OpsFlow St, Tech City",
                    "contact@opsflow.com",
                    "+1234567890",
                    10
            );
            organizationService.create(defaultOrg);
            System.out.println("Organización por defecto creada: OpsFlow Central");
        }
    }
}
