package com.projectnexus.alignment.application;

import com.projectnexus.alignment.application.dto.*;
import com.projectnexus.alignment.domain.AlignmentExpectation;
import com.projectnexus.alignment.domain.Severity;
import com.projectnexus.alignment.infrastructure.AlignmentExpectationRepository;
import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.application.DataContractService;
import com.projectnexus.contracts.application.dto.DataContractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link AlignmentService}.
 * Validates that linked contracts are published before allowing expectation creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlignmentServiceImpl implements AlignmentService {

    private final AlignmentExpectationRepository repository;
    private final DataContractService dataContractService;

    @Override
    @Transactional
    public AlignmentExpectationResponse create(AlignmentExpectationCreateRequest request) {
        UUID tenantId = requireTenantId();

        DataContractResponse contract = dataContractService.getById(request.dataContractId());
        if (!"PUBLISHED".equals(contract.status())) {
            throw new IllegalStateException("Expectations can only be created against PUBLISHED contracts");
        }

        AlignmentExpectation expectation = new AlignmentExpectation();
        expectation.setTenantId(tenantId);
        expectation.setDataContractId(request.dataContractId());
        expectation.setName(request.name());
        expectation.setDescription(request.description());
        expectation.setSeverity(parseSeverity(request.severity()));
        expectation.setRuleExpression(request.ruleExpression());

        AlignmentExpectation saved = repository.save(expectation);
        log.info("Created AlignmentExpectation id={} for contract={}", saved.getId(), request.dataContractId());
        return toResponse(saved);
    }

    @Override
    public AlignmentExpectationResponse getById(UUID id) {
        return toResponse(findByIdForTenant(id));
    }

    @Override
    public Page<AlignmentExpectationResponse> listForCurrentTenant(Pageable pageable) {
        return repository.findByTenantId(requireTenantId(), pageable).map(this::toResponse);
    }

    @Override
    public Page<AlignmentExpectationResponse> listByContract(UUID dataContractId, Pageable pageable) {
        return repository.findByTenantIdAndDataContractId(requireTenantId(), dataContractId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse update(UUID id, AlignmentExpectationUpdateRequest request) {
        AlignmentExpectation expectation = findByIdForTenant(id);

        if (request.name() != null) expectation.setName(request.name());
        if (request.description() != null) expectation.setDescription(request.description());
        if (request.severity() != null) expectation.setSeverity(parseSeverity(request.severity()));
        if (request.ruleExpression() != null) expectation.setRuleExpression(request.ruleExpression());

        return toResponse(repository.save(expectation));
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse deactivate(UUID id) {
        AlignmentExpectation expectation = findByIdForTenant(id);
        expectation.deactivate();
        return toResponse(repository.save(expectation));
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse activate(UUID id) {
        AlignmentExpectation expectation = findByIdForTenant(id);
        expectation.activate();
        return toResponse(repository.save(expectation));
    }

    private AlignmentExpectation findByIdForTenant(UUID id) {
        return repository.findByIdAndTenantId(id, requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("AlignmentExpectation", id));
    }

    private AlignmentExpectationResponse toResponse(AlignmentExpectation e) {
        return new AlignmentExpectationResponse(
                e.getId(), e.getTenantId(), e.getDataContractId(),
                e.getName(), e.getDescription(), e.getSeverity().name(),
                e.getRuleExpression(), e.isActive(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy());
    }

    private Severity parseSeverity(String severity) {
        try {
            return Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid severity: '" + severity + "'. Must be WARNING or CRITICAL.");
        }
    }

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenantId;
    }
}
