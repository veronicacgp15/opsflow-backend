package com.opsflow.auth_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

@Configuration
public class JpaConfig {

    @Bean
    @DependsOn("createSchema")
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {};
    }
}