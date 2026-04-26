package com.projectnexus.alignment.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for an Alignment Expectation.
 */
public record AlignmentExpectationResponse(
        UUID id,
        UUID tenantId,
        UUID dataContractId,
        String name,
        String description,
        String severity,
        String ruleExpression,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {}
