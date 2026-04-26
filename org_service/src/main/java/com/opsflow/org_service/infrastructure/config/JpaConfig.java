package com.opsflow.org_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;

@Configuration
public class JpaConfig {

    @Bean
    @DependsOn("mscOrgSchemaInitializer")
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {};
    }
}