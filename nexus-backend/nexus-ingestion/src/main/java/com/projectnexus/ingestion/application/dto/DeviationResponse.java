package com.projectnexus.ingestion.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response representing a detected deviation for a payload.
 */
public record DeviationResponse(
        UUID id,
        UUID payloadId,
        UUID expectationId,
        UUID dataContractId,
        String severity,
        String description,
        String detectedValue,
        String expectedValue,
        Instant createdAt
) {}
