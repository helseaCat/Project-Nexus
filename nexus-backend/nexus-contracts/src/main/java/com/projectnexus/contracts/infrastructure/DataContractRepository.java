package com.projectnexus.contracts.infrastructure;

import com.projectnexus.contracts.domain.DataContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataContractRepository extends JpaRepository<DataContract, UUID> {

    List<DataContract> findByTenantId(UUID tenantId);
}
