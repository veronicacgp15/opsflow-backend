package com.opsflow.org_service.domain.ports.out;

import com.opsflow.org_service.domain.models.LocationDomain;

import java.util.List;
import java.util.Optional;

public interface LocationRepositoryPort {
    LocationDomain save(LocationDomain location);
    Optional<LocationDomain> findById(Long id);
    List<LocationDomain> findAll();
    List<LocationDomain> findByOrganizationId(Long organizationId);
    void deleteById(Long id);
}
