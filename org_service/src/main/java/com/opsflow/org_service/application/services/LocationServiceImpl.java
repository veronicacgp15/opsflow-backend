package com.opsflow.org_service.application.services;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.application.dtos.request.LocationRequest;
import com.opsflow.org_service.domain.models.LocationDomain;
import com.opsflow.org_service.domain.ports.in.LocationServicePort;
import com.opsflow.org_service.domain.ports.out.LocationRepositoryPort;
import com.opsflow.org_service.infrastructure.mappers.LocationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationServicePort {

    private final LocationRepositoryPort locationRepositoryPort;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(LocationRepositoryPort locationRepositoryPort, LocationMapper locationMapper) {
        this.locationRepositoryPort = locationRepositoryPort;
        this.locationMapper = locationMapper;
    }

    @Override
    @Transactional
    public LocationResponse create(LocationRequest request) {
        LocationDomain domain = new LocationDomain();
        domain.setName(request.name());
        domain.setAddress(request.address());
        domain.setCity(request.city());
        domain.setOrganizationId(request.organizationId());

        LocationDomain savedDomain = locationRepositoryPort.save(domain);
        return locationMapper.toResponse(savedDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationResponse> findById(Long id) {
        return locationRepositoryPort.findById(id).map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findAll() {
        return locationRepositoryPort.findAll().stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findByOrganizationId(Long organizationId) {
        return locationRepositoryPort.findByOrganizationId(organizationId).stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public Optional<LocationResponse> update(Long id, LocationRequest request) {
        return locationRepositoryPort.findById(id).map(existingDomain -> {
            existingDomain.setName(request.name());
            existingDomain.setAddress(request.address());
            existingDomain.setCity(request.city());
            existingDomain.setOrganizationId(request.organizationId());
            
            LocationDomain updatedDomain = locationRepositoryPort.save(existingDomain);
            return locationMapper.toResponse(updatedDomain);
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (locationRepositoryPort.findById(id).isPresent()) {
            locationRepositoryPort.deleteById(id);
            return true;
        }
        return false;
    }
}
