package com.projectnexus.contracts.application;

import com.projectnexus.contracts.application.dto.DataContractCreateRequest;
import com.projectnexus.contracts.application.dto.DataContractResponse;
import com.projectnexus.contracts.application.dto.DataContractUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Application service for Data Contract use cases.
 *
 * <p>All operations are tenant-scoped — the current tenant is resolved
 * from {@link com.projectnexus.common.tenant.TenantContext}.
 */
public interface DataContractService {

    DataContractResponse create(DataContractCreateRequest request);

    DataContractResponse getById(UUID id);

    Page<DataContractResponse> listForCurrentTenant(Pageable pageable);

    Page<DataContractResponse> listPublished(Pageable pageable);

    DataContractResponse update(UUID id, DataContractUpdateRequest request);

    DataContractResponse publish(UUID id);
}
