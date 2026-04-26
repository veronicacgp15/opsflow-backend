package com.opsflow.org_service.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.opsflow.org_service.domain.constants.OrgConstants.AUTHORIZATION;
import static com.opsflow.org_service.domain.constants.OrgConstants.BEARER;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String jwtToken = (String) authentication.getCredentials();

            if (jwtToken != null) {
                template.header(AUTHORIZATION, String.format("%s %s", BEARER, jwtToken));
            }
        }
    }
}
