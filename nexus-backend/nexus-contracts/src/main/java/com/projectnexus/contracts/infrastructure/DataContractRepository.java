package com.projectnexus.contracts.infrastructure;

import com.projectnexus.contracts.domain.ContractStatus;
import com.projectnexus.contracts.domain.DataContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link DataContract} persistence operations.
 *
 * <p>All queries are tenant-scoped — they include {@code tenantId} to ensure
 * application-level isolation in addition to PostgreSQL RLS.
 */
@Repository
public interface DataContractRepository extends JpaRepository<DataContract, UUID> {

    Page<DataContract> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<DataContract> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<DataContract> findByTenantIdAndStatus(UUID tenantId, ContractStatus status, Pageable pageable);

    @Query("SELECT dc FROM DataContract dc WHERE dc.tenantId = :tenantId AND dc.name = :name ORDER BY dc.version DESC LIMIT 1")
    Optional<DataContract> findLatestVersionByName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    boolean existsByTenantIdAndNameAndStatus(UUID tenantId, String name, ContractStatus status);
}
