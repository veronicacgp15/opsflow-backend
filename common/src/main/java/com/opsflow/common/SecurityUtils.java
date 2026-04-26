package com.opsflow.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        return getClaimFromDetails("userId");
    }

    public static Long getCurrentOrganizationId() {
        return getClaimFromDetails("organizationId");
    }

    private static Long getClaimFromDetails(String key) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get(key);
            if (value instanceof Number number) return number.longValue();
            if (value != null) return Long.parseLong(value.toString());
        }
        return null;
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
    
    public static String getJwtToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getCredentials() instanceof String token) ? token : null;
    }
}