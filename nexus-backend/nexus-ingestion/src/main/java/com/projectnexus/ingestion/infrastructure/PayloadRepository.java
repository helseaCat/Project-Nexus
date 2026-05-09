package com.projectnexus.ingestion.infrastructure;

import com.projectnexus.alignment.domain.Payload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Tenant-scoped repository for Payload entities.
 */
@Repository
public interface PayloadRepository extends JpaRepository<Payload, UUID> {

    Optional<Payload> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Payload> findByTenantIdAndDataContractId(UUID tenantId, UUID dataContractId, Pageable pageable);
}
