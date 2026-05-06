package com.projectnexus.ingestion.application.dto;

import com.projectnexus.alignment.domain.Severity;

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
        Severity severity,
        String description,
        String detectedValue,
        String expectedValue,
        Instant createdAt
) {}
