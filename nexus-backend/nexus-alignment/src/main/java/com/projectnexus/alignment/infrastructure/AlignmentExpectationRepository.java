package com.projectnexus.alignment.infrastructure;

import com.projectnexus.alignment.domain.AlignmentExpectation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link AlignmentExpectation} persistence operations.
 * All queries are tenant-scoped for application-level isolation.
 */
@Repository
public interface AlignmentExpectationRepository extends JpaRepository<AlignmentExpectation, UUID> {

    Optional<AlignmentExpectation> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<AlignmentExpectation> findByTenantId(UUID tenantId, Pageable pageable);

    Page<AlignmentExpectation> findByTenantIdAndDataContractId(UUID tenantId, UUID dataContractId, Pageable pageable);

    List<AlignmentExpectation> findByDataContractIdAndActiveTrue(UUID dataContractId);

    long countByTenantIdAndDataContractId(UUID tenantId, UUID dataContractId);
}
