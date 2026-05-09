package com.projectnexus.ingestion.application.dto;

import com.projectnexus.alignment.domain.PayloadStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response representing a submitted payload and its processing status.
 */
public record PayloadResponse(
        UUID id,
        UUID tenantId,
        UUID dataContractId,
        PayloadStatus status,
        String s3Key,
        Instant createdAt,
        Instant updatedAt
) {}
