package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.PayloadStatus;
import com.projectnexus.ingestion.infrastructure.PayloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Persists payload failure status in a separate transaction (REQUIRES_NEW).
 * This ensures the FAILED status commits even when the outer transaction rolls back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayloadFailureLogger {

    private final PayloadRepository payloadRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID payloadId, UUID tenantId) {
        payloadRepository.findByIdAndTenantId(payloadId, tenantId)
                .ifPresent(p -> {
                    p.setStatus(PayloadStatus.FAILED);
                    payloadRepository.save(p);
                    log.warn("Marked payload id={} as FAILED in separate transaction", payloadId);
                });
    }
}
