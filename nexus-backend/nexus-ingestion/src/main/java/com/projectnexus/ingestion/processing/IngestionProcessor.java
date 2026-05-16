package com.projectnexus.ingestion.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectnexus.alignment.domain.AlignmentExpectation;
import com.projectnexus.alignment.domain.Payload;
import com.projectnexus.alignment.domain.PayloadStatus;
import com.projectnexus.alignment.infrastructure.AlignmentExpectationRepository;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.domain.DataContract;
import com.projectnexus.contracts.infrastructure.DataContractRepository;
import com.projectnexus.ingestion.application.dto.IngestionMessage;
import com.projectnexus.ingestion.application.dto.Violation;
import com.projectnexus.ingestion.infrastructure.PayloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Async RabbitMQ consumer that orchestrates the full payload validation pipeline.
 *
 * <p>Flow:
 * <ol>
 *   <li>Set tenant context from message</li>
 *   <li>Load payload, transition to PROCESSING</li>
 *   <li>Validate against Data Contract test variables</li>
 *   <li>Evaluate active Alignment Expectations</li>
 *   <li>Record deviations</li>
 *   <li>Transition to VALIDATED or FAILED</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionProcessor {

    private final PayloadRepository payloadRepository;
    private final DataContractRepository dataContractRepository;
    private final AlignmentExpectationRepository expectationRepository;
    private final PayloadValidator payloadValidator;
    private final ExpectationEvaluator expectationEvaluator;
    private final DeviationRecorder deviationRecorder;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "nexus.payloads.ingestion")
    @Transactional
    public void process(IngestionMessage message) {
        UUID payloadId = message.payloadId();
        UUID tenantId = message.tenantId();

        try {
            TenantContext.setCurrentTenant(tenantId);
            log.info("Processing payload id={} for tenant={}", payloadId, tenantId);

            // Load payload
            Payload payload = payloadRepository.findByIdAndTenantId(payloadId, tenantId)
                    .orElse(null);
            if (payload == null) {
                log.warn("Payload id={} not found for tenant={}, acknowledging message", payloadId, tenantId);
                return;
            }

            // Transition to PROCESSING
            payload.setStatus(PayloadStatus.PROCESSING);
            payloadRepository.save(payload);

            // Load contract
            DataContract contract = dataContractRepository.findByIdAndTenantId(
                    payload.getDataContractId(), tenantId).orElse(null);
            if (contract == null) {
                log.error("DataContract id={} not found for payload={}", payload.getDataContractId(), payloadId);
                payload.setStatus(PayloadStatus.FAILED);
                payloadRepository.save(payload);
                return;
            }

            // Parse raw payload to Map
            Map<String, Object> rawPayloadMap = parsePayload(payload.getRawPayload());

            // Step 1: Validate against contract test variables
            List<Violation> violations = new ArrayList<>(
                    payloadValidator.validate(rawPayloadMap, contract.getTestVariables()));

            // Step 2: Evaluate active alignment expectations
            List<AlignmentExpectation> activeExpectations =
                    expectationRepository.findByDataContractIdAndActiveTrue(contract.getId());
            violations.addAll(expectationEvaluator.evaluate(rawPayloadMap, activeExpectations));

            // Step 3: Record deviations
            if (!violations.isEmpty()) {
                deviationRecorder.record(payloadId, contract.getId(), tenantId, violations);
            }

            // TODO: Step 4 — AI-enhanced detection (DeviationDetector interface has no methods yet)

            // Transition to final status
            payload.setStatus(violations.isEmpty() ? PayloadStatus.VALIDATED : PayloadStatus.FAILED);
            payloadRepository.save(payload);

            log.info("Payload id={} processing complete: status={}, violations={}",
                    payloadId, payload.getStatus(), violations.size());

        } catch (Exception e) {
            log.error("Unexpected error processing payload id={}: {}", payloadId, e.getMessage(), e);
            // Attempt to mark as FAILED
            try {
                payloadRepository.findByIdAndTenantId(payloadId, tenantId)
                        .ifPresent(p -> {
                            p.setStatus(PayloadStatus.FAILED);
                            payloadRepository.save(p);
                        });
            } catch (Exception inner) {
                log.error("Failed to mark payload as FAILED: {}", inner.getMessage());
            }
            throw e; // Re-throw for RabbitMQ retry/DLQ handling
        } finally {
            TenantContext.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(Object rawPayload) {
        if (rawPayload instanceof Map) {
            return (Map<String, Object>) rawPayload;
        }
        try {
            return objectMapper.convertValue(rawPayload, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse rawPayload to Map, returning empty: {}", e.getMessage());
            return Map.of();
        }
    }
}
