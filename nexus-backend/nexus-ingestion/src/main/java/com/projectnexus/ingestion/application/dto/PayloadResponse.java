package com.projectnexus.ingestion.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response representing a submitted payload and its processing status.
 */
public record PayloadResponse(
        UUID id,
        UUID tenantId,
        UUID dataContractId,
        String status,
        String s3Key,
        Instant createdAt,
        Instant updatedAt
) {}
