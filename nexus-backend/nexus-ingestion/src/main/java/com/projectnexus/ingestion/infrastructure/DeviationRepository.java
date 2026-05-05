package com.projectnexus.ingestion.infrastructure;

import com.projectnexus.alignment.domain.Deviation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Tenant-scoped repository for Deviation entities.
 */
@Repository
public interface DeviationRepository extends JpaRepository<Deviation, UUID> {

    Page<Deviation> findByPayloadIdAndTenantId(UUID payloadId, UUID tenantId, Pageable pageable);

    Page<Deviation> findByDataContractIdAndTenantId(UUID dataContractId, UUID tenantId, Pageable pageable);
}
