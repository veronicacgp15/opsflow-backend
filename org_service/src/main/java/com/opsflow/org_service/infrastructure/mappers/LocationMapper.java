package com.opsflow.org_service.infrastructure.mappers;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.domain.models.LocationDomain;
import com.opsflow.org_service.infrastructure.entities.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationDomain toDomain(Location entity) {
        if (entity == null) return null;
        LocationDomain domain = new LocationDomain();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setAddress(entity.getAddress());
        domain.setCity(entity.getCity());
        if (entity.getOrganization() != null) {
            domain.setOrganizationId(entity.getOrganization().getId());
        }
        return domain;
    }

    public Location toEntity(LocationDomain domain) {
        if (domain == null) return null;
        Location entity = new Location();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setCity(domain.getCity());

        return entity;
    }

    public LocationResponse toResponse(LocationDomain domain) {
        if (domain == null) return null;
        return new LocationResponse(
                domain.getId(),
                domain.getName(),
                domain.getAddress(),
                domain.getCity(),
                domain.getOrganizationId()
        );
    }
}
