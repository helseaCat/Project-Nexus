package com.projectnexus.ingestion.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectnexus.alignment.domain.Deviation;
import com.projectnexus.alignment.domain.Payload;
import com.projectnexus.alignment.domain.PayloadStatus;
import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.domain.ContractStatus;
import com.projectnexus.contracts.infrastructure.DataContractRepository;
import com.projectnexus.ingestion.application.dto.*;
import com.projectnexus.ingestion.infrastructure.DeviationRepository;
import com.projectnexus.ingestion.infrastructure.PayloadRepository;
import com.projectnexus.ingestion.infrastructure.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Orchestrates synchronous payload submission:
 * validates contract, creates entity, uploads to S3, publishes to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayloadServiceImpl implements PayloadService {

    private static final String EXCHANGE = "nexus.payloads";
    private static final String ROUTING_KEY = "payload.submitted";

    private final PayloadRepository payloadRepository;
    private final DeviationRepository deviationRepository;
    private final DataContractRepository dataContractRepository;
    private final S3StorageService s3StorageService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PayloadResponse submit(PayloadSubmitRequest request) {
        UUID tenantId = requireTenantId();

        var contract = dataContractRepository.findByIdAndTenantId(request.dataContractId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DataContract", request.dataContractId()));

        if (contract.getStatus() != ContractStatus.PUBLISHED) {
            throw new IllegalStateException("Data Contract must be in PUBLISHED status to accept payloads");
        }

        Payload payload = new Payload();
        payload.setTenantId(tenantId);
        payload.setDataContractId(request.dataContractId());
        payload.setRawPayload(request.rawPayload());
        payload.setStatus(PayloadStatus.PENDING);

        Payload saved = payloadRepository.save(payload);

        // Best-effort S3 upload
        String jsonContent = serializePayload(request.rawPayload());
        s3StorageService.upload(tenantId, request.dataContractId(), saved.getId(), jsonContent)
                .ifPresent(key -> {
                    saved.setS3Key(key);
                    payloadRepository.save(saved);
                });

        // Publish to RabbitMQ for async processing
        var message = new IngestionMessage(saved.getId(), tenantId);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        log.info("Submitted payload id={} for contract={} tenant={}", saved.getId(), request.dataContractId(), tenantId);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PayloadResponse getById(UUID id) {
        UUID tenantId = requireTenantId();
        Payload payload = payloadRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payload", id));
        return toResponse(payload);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayloadResponse> listByContract(UUID contractId, Pageable pageable) {
        UUID tenantId = requireTenantId();
        return payloadRepository.findByTenantIdAndDataContractId(tenantId, contractId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviationResponse> getDeviationsForPayload(UUID payloadId, Pageable pageable) {
        UUID tenantId = requireTenantId();
        return deviationRepository.findByPayloadIdAndTenantId(payloadId, tenantId, pageable)
                .map(this::toDeviationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviationResponse> listDeviationsByContract(UUID contractId, Pageable pageable) {
        UUID tenantId = requireTenantId();
        return deviationRepository.findByDataContractIdAndTenantId(contractId, tenantId, pageable)
                .map(this::toDeviationResponse);
    }

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenantId;
    }

    private String serializePayload(Object rawPayload) {
        try {
            return objectMapper.writeValueAsString(rawPayload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload for S3, using toString()", e);
            return rawPayload.toString();
        }
    }

    private PayloadResponse toResponse(Payload p) {
        return new PayloadResponse(
                p.getId(), p.getTenantId(), p.getDataContractId(),
                p.getStatus(), p.getS3Key(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    private DeviationResponse toDeviationResponse(Deviation d) {
        return new DeviationResponse(
                d.getId(), d.getPayloadId(), d.getExpectationId(),
                d.getDataContractId(), d.getSeverity(),
                d.getDescription(), d.getDetectedValue(), d.getExpectedValue(),
                d.getCreatedAt());
    }
}
