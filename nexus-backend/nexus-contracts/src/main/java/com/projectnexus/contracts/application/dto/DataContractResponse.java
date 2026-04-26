package com.projectnexus.contracts.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response payload for a Data Contract.
 */
public record DataContractResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        String status,
        Object businessGoals,
        Object sharingRules,
        List<TestVariableResponse> testVariables,
        int version,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {}
