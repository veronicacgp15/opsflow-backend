package com.opsflow.org_service.infrastructure.repositories;

import com.opsflow.org_service.infrastructure.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByOrganizationId(Long organizationId);
}
