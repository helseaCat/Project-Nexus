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
 *
 * <p>Handles creation, update, activation/deactivation, and retrieval of
 * Alignment Expectations with full tenant isolation. Validates that the
 * linked Data Contract exists and is published before allowing creation.
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

        // Verify the linked contract exists and is published
        DataContractResponse contract = dataContractService.getById(request.dataContractId());
        if (!"PUBLISHED".equals(contract.status())) {
            throw new IllegalStateException("Expectations can only be created against PUBLISHED contracts");
        }

        Severity severity = parseSeverity(request.severity());

        AlignmentExpectation expectation = new AlignmentExpectation();
        expectation.setTenantId(tenantId);
        expectation.setDataContractId(request.dataContractId());
        expectation.setName(request.name());
        expectation.setDescription(request.description());
        expectation.setSeverity(severity);
        expectation.setRuleExpression(request.ruleExpression());

        AlignmentExpectation saved = repository.save(expectation);
        log.info("Created AlignmentExpectation id={} for contract={} tenant={}", saved.getId(), request.dataContractId(), tenantId);
        return toResponse(saved);
    }

    @Override
    public AlignmentExpectationResponse getById(UUID id) {
        UUID tenantId = requireTenantId();
        AlignmentExpectation expectation = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AlignmentExpectation", id));
        return toResponse(expectation);
    }

    @Override
    public Page<AlignmentExpectationResponse> listForCurrentTenant(Pageable pageable) {
        UUID tenantId = requireTenantId();
        return repository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Override
    public Page<AlignmentExpectationResponse> listByContract(UUID dataContractId, Pageable pageable) {
        UUID tenantId = requireTenantId();
        return repository.findByTenantIdAndDataContractId(tenantId, dataContractId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse update(UUID id, AlignmentExpectationUpdateRequest request) {
        UUID tenantId = requireTenantId();
        AlignmentExpectation expectation = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AlignmentExpectation", id));

        if (request.name() != null) {
            expectation.setName(request.name());
        }
        if (request.description() != null) {
            expectation.setDescription(request.description());
        }
        if (request.severity() != null) {
            expectation.setSeverity(parseSeverity(request.severity()));
        }
        if (request.ruleExpression() != null) {
            expectation.setRuleExpression(request.ruleExpression());
        }

        AlignmentExpectation saved = repository.save(expectation);
        log.info("Updated AlignmentExpectation id={} for tenant={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse deactivate(UUID id) {
        UUID tenantId = requireTenantId();
        AlignmentExpectation expectation = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AlignmentExpectation", id));

        expectation.deactivate();
        AlignmentExpectation saved = repository.save(expectation);
        log.info("Deactivated AlignmentExpectation id={} for tenant={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlignmentExpectationResponse activate(UUID id) {
        UUID tenantId = requireTenantId();
        AlignmentExpectation expectation = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AlignmentExpectation", id));

        expectation.activate();
        AlignmentExpectation saved = repository.save(expectation);
        log.info("Activated AlignmentExpectation id={} for tenant={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    // ---- Helpers ----

    private AlignmentExpectationResponse toResponse(AlignmentExpectation e) {
        return new AlignmentExpectationResponse(
                e.getId(),
                e.getTenantId(),
                e.getDataContractId(),
                e.getName(),
                e.getDescription(),
                e.getSeverity().name(),
                e.getRuleExpression(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getCreatedBy()
        );
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
            throw new IllegalStateException("No tenant context available. Ensure the request includes a valid JWT.");
        }
        return tenantId;
    }
}
