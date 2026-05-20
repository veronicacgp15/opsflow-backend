package com.opsflow.gateway_service.filter;

import com.opsflow.gateway_service.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationGatewayFilter extends AbstractGatewayFilterFactory<AuthenticationGatewayFilter.Config> {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/auth/login",
            "/auth/signup",
            "/auth/verify",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/refresh",
            "/auth-legacy/login",
            "/auth-legacy/signup",
            "/auth-legacy/refresh",
            "/swagger-ui",
            "/v3/api-docs",
            "/auth/v3/api-docs",
            "/auth/swagger-ui",
            "/org/v3/api-docs",
            "/org/swagger-ui",
            "/documents/v3/api-docs",
            "/documents/swagger-ui"
    );

    private final JwtUtil jwtUtil;

    public AuthenticationGatewayFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                return chain.filter(exchange);
            }

            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }

            String token = authorization.substring("Bearer ".length()).trim();
            if (!jwtUtil.isValid(token)) {
                return unauthorized(exchange);
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", jwtUtil.getUserId(token))
                    .header("X-Org-Id", jwtUtil.getOrgId(token))
                    .header("X-User-Role", jwtUtil.getRoles(token))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        };
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private static Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
