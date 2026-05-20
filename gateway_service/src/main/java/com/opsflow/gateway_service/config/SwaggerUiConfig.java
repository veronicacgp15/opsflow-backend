package com.opsflow.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

@Configuration
public class SwaggerUiConfig {

    @Bean
    @Order(-1)
    public RouterFunction<ServerResponse> swaggerRedirects() {
        return RouterFunctions.route()
                .GET("/swagger-ui/index.html", request ->
                        ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create("/swagger-ui.html"))
                                .build())
                .build();
    }
}
