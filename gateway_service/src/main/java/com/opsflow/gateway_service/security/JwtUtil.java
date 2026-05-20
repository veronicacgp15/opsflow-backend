package com.opsflow.gateway_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ORGANIZATION_ID = "organizationId";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserId(String token) {
        return stringifyClaim(extractClaims(token).get(CLAIM_USER_ID));
    }

    public String getOrgId(String token) {
        return stringifyClaim(extractClaims(token).get(CLAIM_ORGANIZATION_ID));
    }

    @SuppressWarnings("unchecked")
    public String getRoles(String token) {
        Object roles = extractClaims(token).get(CLAIM_ROLES);
        if (roles == null) {
            return "";
        }
        if (roles instanceof List<?> list) {
            return String.join(",", list.stream().map(Object::toString).toList());
        }
        return roles.toString();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String stringifyClaim(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number number) {
            return number.toString();
        }
        return value.toString();
    }
}
