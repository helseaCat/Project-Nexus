package com.projectnexus.ingestion.application;

import com.projectnexus.ingestion.application.dto.DeviationResponse;
import com.projectnexus.ingestion.application.dto.PayloadResponse;
import com.projectnexus.ingestion.application.dto.PayloadSubmitRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Application service for payload submission and query operations.
 *
 * <p>Submission is synchronous (creates entity, uploads to S3, publishes to RabbitMQ).
 * All queries are tenant-scoped.
 */
public interface PayloadService {

    PayloadResponse submit(PayloadSubmitRequest request);

    PayloadResponse getById(UUID id);

    Page<PayloadResponse> listByContract(UUID contractId, Pageable pageable);

    Page<DeviationResponse> getDeviationsForPayload(UUID payloadId, Pageable pageable);

    Page<DeviationResponse> listDeviationsByContract(UUID contractId, Pageable pageable);
}
