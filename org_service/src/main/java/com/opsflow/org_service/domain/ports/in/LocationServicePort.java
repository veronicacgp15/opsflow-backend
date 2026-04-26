package com.opsflow.org_service.domain.ports.in;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.application.dtos.request.LocationRequest;

import java.util.List;
import java.util.Optional;

public interface LocationServicePort {
    LocationResponse create(LocationRequest request);
    Optional<LocationResponse> findById(Long id);
    List<LocationResponse> findAll();
    List<LocationResponse> findByOrganizationId(Long organizationId);
    Optional<LocationResponse> update(Long id, LocationRequest request); // Añadido
    boolean delete(Long id);
}
