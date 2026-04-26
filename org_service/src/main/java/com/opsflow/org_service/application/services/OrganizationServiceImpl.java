package com.opsflow.org_service.application.services;

import com.opsflow.org_service.application.dtos.request.OrganizationRequest;
import com.opsflow.org_service.application.dtos.OrganizationResponse;
import com.opsflow.org_service.domain.models.OrganizationDomain;
import com.opsflow.org_service.domain.ports.in.OrganizationServicePort;
import com.opsflow.org_service.domain.ports.out.OrganizationRepositoryPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationServicePort {

    private final OrganizationRepositoryPort organizationRepositoryPort;

    public OrganizationServiceImpl(OrganizationRepositoryPort organizationRepositoryPort) {
        this.organizationRepositoryPort = organizationRepositoryPort;
    }

    @Override
    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        OrganizationDomain domain = new OrganizationDomain();
        domain.setName(request.name());
        domain.setTaxId(request.taxId());
        domain.setAddress(request.address());
        domain.setEmail(request.email());
        domain.setPhone(request.phone());
        domain.setActive(true);
        domain.setPlanLimit(request.planLimit() != null ? request.planLimit() : 10);
        domain.setCreatedAt(LocalDateTime.now());

        OrganizationDomain savedDomain = organizationRepositoryPort.save(domain);
        return toResponse(savedDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationResponse> findById(Long id) {
        return organizationRepositoryPort.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll() {
        return organizationRepositoryPort.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        organizationRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<OrganizationResponse> update(Long id, OrganizationRequest request) {
        return organizationRepositoryPort.findById(id).map(domain -> {
            domain.setName(request.name());
            domain.setTaxId(request.taxId());
            domain.setAddress(request.address());
            domain.setEmail(request.email());
            domain.setPhone(request.phone());

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin && request.planLimit() != null) {
                domain.setPlanLimit(request.planLimit());
            }

            OrganizationDomain updatedDomain = organizationRepositoryPort.save(domain);
            return toResponse(updatedDomain);
        });
    }

    private OrganizationResponse toResponse(OrganizationDomain domain) {
        return new OrganizationResponse(
                domain.getId(),
                domain.getName(),
                domain.getTaxId(),
                domain.getAddress(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getActive(),
                domain.getPlanLimit(),
                domain.getCreatedAt()
        );
    }
}
