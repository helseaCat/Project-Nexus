package com.projectnexus.contracts.application;

import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.application.dto.*;
import com.projectnexus.contracts.domain.ContractStatus;
import com.projectnexus.contracts.domain.DataContract;
import com.projectnexus.contracts.domain.TestVariable;
import com.projectnexus.contracts.infrastructure.DataContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link DataContractService}.
 *
 * <p>Handles creation, update, publishing, and retrieval of Data Contracts
 * with full tenant isolation via {@link TenantContext}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataContractServiceImpl implements DataContractService {

    private final DataContractRepository repository;

    @Override
    @Transactional
    public DataContractResponse create(DataContractCreateRequest request) {
        UUID tenantId = requireTenantId();

        DataContract contract = new DataContract();
        contract.setTenantId(tenantId);
        contract.setName(request.name());
        contract.setDescription(request.description());
        contract.setBusinessGoals(request.businessGoals());
        contract.setSharingRules(request.sharingRules());

        if (request.testVariables() != null) {
            for (TestVariableRequest tvr : request.testVariables()) {
                validateVariableRange(tvr);
                TestVariable tv = mapToTestVariable(tvr, tenantId);
                contract.addTestVariable(tv);
            }
        }

        DataContract saved = repository.save(contract);
        log.info("Created DataContract id={} name='{}' for tenant={}", saved.getId(), saved.getName(), tenantId);
        return toResponse(saved);
    }

    @Override
    public DataContractResponse getById(UUID id) {
        UUID tenantId = requireTenantId();
        DataContract contract = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DataContract", id));
        return toResponse(contract);
    }

    @Override
    public Page<DataContractResponse> listForCurrentTenant(Pageable pageable) {
        UUID tenantId = requireTenantId();
        return repository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Override
    public Page<DataContractResponse> listPublished(Pageable pageable) {
        UUID tenantId = requireTenantId();
        return repository.findByTenantIdAndStatus(tenantId, ContractStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public DataContractResponse update(UUID id, DataContractUpdateRequest request) {
        UUID tenantId = requireTenantId();
        DataContract contract = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DataContract", id));

        if (contract.getStatus() == ContractStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a published contract. Create a new version instead.");
        }

        if (request.name() != null) {
            contract.setName(request.name());
        }
        if (request.description() != null) {
            contract.setDescription(request.description());
        }
        if (request.businessGoals() != null) {
            contract.setBusinessGoals(request.businessGoals());
        }
        if (request.sharingRules() != null) {
            contract.setSharingRules(request.sharingRules());
        }

        if (request.testVariables() != null) {
            contract.getTestVariables().clear();
            for (TestVariableRequest tvr : request.testVariables()) {
                validateVariableRange(tvr);
                TestVariable tv = mapToTestVariable(tvr, tenantId);
                contract.addTestVariable(tv);
            }
        }

        DataContract saved = repository.save(contract);
        log.info("Updated DataContract id={} for tenant={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DataContractResponse publish(UUID id) {
        UUID tenantId = requireTenantId();
        DataContract contract = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DataContract", id));

        contract.publish();

        DataContract saved = repository.save(contract);
        log.info("Published DataContract id={} version={} for tenant={}", saved.getId(), saved.getVersion(), tenantId);
        return toResponse(saved);
    }

    // ---- Mapping helpers ----

    private DataContractResponse toResponse(DataContract contract) {
        List<TestVariableResponse> variables = contract.getTestVariables().stream()
                .map(tv -> new TestVariableResponse(
                        tv.getId(),
                        tv.getName(),
                        tv.getDataType(),
                        tv.getUnit(),
                        tv.getMinValue(),
                        tv.getMaxValue(),
                        tv.getQualityRules()
                ))
                .toList();

        return new DataContractResponse(
                contract.getId(),
                contract.getTenantId(),
                contract.getName(),
                contract.getDescription(),
                contract.getStatus().name(),
                contract.getBusinessGoals(),
                contract.getSharingRules(),
                variables,
                contract.getVersion(),
                contract.getPublishedAt(),
                contract.getCreatedAt(),
                contract.getUpdatedAt(),
                contract.getCreatedBy()
        );
    }

    private TestVariable mapToTestVariable(TestVariableRequest request, UUID tenantId) {
        TestVariable tv = new TestVariable();
        tv.setTenantId(tenantId);
        tv.setName(request.name());
        tv.setDataType(request.dataType());
        tv.setUnit(request.unit());
        tv.setMinValue(request.minValue());
        tv.setMaxValue(request.maxValue());
        tv.setQualityRules(request.qualityRules());
        return tv;
    }

    private void validateVariableRange(TestVariableRequest request) {
        if (request.minValue() != null && request.maxValue() != null
                && request.minValue() > request.maxValue()) {
            throw new IllegalArgumentException(
                    "minValue (" + request.minValue() + ") cannot be greater than maxValue (" + request.maxValue() + ") for variable '" + request.name() + "'");
        }
    }

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available. Ensure the request includes a valid JWT.");
        }
        return tenantId;
    }
}
