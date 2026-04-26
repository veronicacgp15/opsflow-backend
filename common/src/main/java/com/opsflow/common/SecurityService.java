package com.opsflow.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("securityService")
public class SecurityService {

    private final JwtUtils jwtUtils;

    public SecurityService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public boolean isMemberOfOrganization(Long organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        Long userOrgId = jwtUtils.getOrganizationIdFromAuthentication(auth);
        return Objects.equals(userOrgId, organizationId);
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return jwtUtils.hasRole(auth, role);
    }
    
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return jwtUtils.getUserIdFromAuthentication(auth);
    }
}