package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.Deviation;
import com.projectnexus.ingestion.application.dto.Violation;
import com.projectnexus.ingestion.infrastructure.DeviationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Maps each {@link Violation} to a {@link Deviation} entity and batch-saves them.
 *
 * <p>The tenantId is passed explicitly (not from TenantContext) because this
 * component is called from the async processor which sets context from the message.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviationRecorderImpl implements DeviationRecorder {

    private final DeviationRepository deviationRepository;

    @Override
    @Transactional
    public List<Deviation> record(UUID payloadId, UUID contractId, UUID tenantId, List<Violation> violations) {
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }

        List<Deviation> deviations = violations.stream()
                .map(v -> toDeviation(v, payloadId, contractId, tenantId))
                .toList();

        List<Deviation> saved = deviationRepository.saveAll(deviations);
        log.info("Recorded {} deviation(s) for payload={} contract={} tenant={}",
                saved.size(), payloadId, contractId, tenantId);
        return saved;
    }

    private Deviation toDeviation(Violation violation, UUID payloadId, UUID contractId, UUID tenantId) {
        Deviation d = new Deviation();
        d.setPayloadId(payloadId);
        d.setDataContractId(contractId);
        d.setTenantId(tenantId);
        d.setSeverity(violation.severity());
        d.setDescription(violation.description());
        d.setDetectedValue(violation.detectedValue());
        d.setExpectedValue(violation.expectedValue());
        d.setExpectationId(violation.expectationId()); // null for contract violations
        return d;
    }
}
