package com.projectnexus.alignment.application;

import com.projectnexus.alignment.application.dto.AlignmentExpectationCreateRequest;
import com.projectnexus.alignment.application.dto.AlignmentExpectationResponse;
import com.projectnexus.alignment.application.dto.AlignmentExpectationUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Application service for Alignment Expectation use cases.
 *
 * <p>All operations are tenant-scoped — the current tenant is resolved
 * from {@link com.projectnexus.common.tenant.TenantContext}.
 */
public interface AlignmentService {

    AlignmentExpectationResponse create(AlignmentExpectationCreateRequest request);

    AlignmentExpectationResponse getById(UUID id);

    Page<AlignmentExpectationResponse> listForCurrentTenant(Pageable pageable);

    Page<AlignmentExpectationResponse> listByContract(UUID dataContractId, Pageable pageable);

    AlignmentExpectationResponse update(UUID id, AlignmentExpectationUpdateRequest request);

    AlignmentExpectationResponse deactivate(UUID id);

    AlignmentExpectationResponse activate(UUID id);
}
