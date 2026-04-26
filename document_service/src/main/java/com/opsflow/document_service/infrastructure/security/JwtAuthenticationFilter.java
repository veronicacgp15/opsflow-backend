package com.opsflow.document_service.infrastructure.security;

import com.opsflow.common.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (jwtUtils.validateAccessToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(jwt, request);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    private void authenticateUser(String jwt, HttpServletRequest request) {
        String username = jwtUtils.getUsernameFromToken(jwt);
        List<String> roles = jwtUtils.getRolesFromToken(jwt);
        Long organizationId = jwtUtils.getOrganizationIdFromToken(jwt);
        Long userId = jwtUtils.getUserIdFromToken(jwt);

        if (username == null) return;

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", organizationId);
        details.put("userId", userId);
        details.put("remoteAddress", request.getRemoteAddr());
        details.put("sessionId", request.getSession(false) != null ? request.getSession(false).getId() : null);

        authToken.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
