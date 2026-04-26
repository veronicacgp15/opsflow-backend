package com.opsflow.org_service.domain.ports.in;

import com.opsflow.org_service.application.dtos.request.OrganizationRequest;
import com.opsflow.org_service.application.dtos.OrganizationResponse;
import java.util.List;
import java.util.Optional;

public interface OrganizationServicePort {
    OrganizationResponse create(OrganizationRequest request);
    Optional<OrganizationResponse> findById(Long id);
    List<OrganizationResponse> findAll();
    void delete(Long id);
    Optional<OrganizationResponse> update(Long id, OrganizationRequest request);
}
