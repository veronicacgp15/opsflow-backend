package com.opsflow.auth_service.infrastructure.security;

import com.opsflow.common.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ORG_ID = "X-Org-Id";

    private final JwtUtils jwtUtils;
    private final JpaUserDetailsService userDetailsService;

    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    public AuthTokenFilter(JwtUtils jwtUtils, JpaUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (EXCLUDE_URLS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!authenticateFromBearerToken(request) && !authenticateFromGatewayHeaders(request)) {
                logger.debug("Sin autenticacion para " + request.getMethod() + " " + path);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: " + e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateFromBearerToken(HttpServletRequest request) {
        String jwt = parseJwt(request);
        if (jwt == null || !jwtUtils.validateAccessToken(jwt)) {
            return false;
        }

        String username = jwtUtils.getUsernameFromToken(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        setSecurityContext(request, userDetails, jwtUtils.getOrganizationIdFromToken(jwt),
                jwtUtils.getUserIdFromToken(jwt));
        return true;
    }


    private boolean authenticateFromGatewayHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(userIdHeader)) {
            return false;
        }

        Long userId = Long.parseLong(userIdHeader.trim());
        UserDetails userDetails = userDetailsService.loadUserById(userId);

        Long orgId = parseLongHeader(request.getHeader(HEADER_ORG_ID));
        setSecurityContext(request, userDetails, orgId, userId);
        return true;
    }

    private void setSecurityContext(HttpServletRequest request,
                                    UserDetails userDetails,
                                    Long organizationId,
                                    Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        Map<String, Object> details = new HashMap<>();
        details.put("webDetails", new WebAuthenticationDetailsSource().buildDetails(request));
        if (organizationId != null) {
            details.put("organizationId", organizationId);
        }
        if (userId != null) {
            details.put("userId", userId);
        }
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Long parseLongHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
