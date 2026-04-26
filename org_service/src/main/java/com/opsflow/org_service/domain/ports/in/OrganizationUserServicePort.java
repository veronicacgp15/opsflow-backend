package com.opsflow.org_service.domain.ports.in;

public interface OrganizationUserServicePort {
    void associateUserToOrganization(Long userId, Long organizationId);
}
