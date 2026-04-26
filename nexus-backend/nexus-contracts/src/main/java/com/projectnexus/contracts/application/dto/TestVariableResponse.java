package com.projectnexus.contracts.application.dto;

import java.util.UUID;

/**
 * Response payload for a test variable.
 */
public record TestVariableResponse(
        UUID id,
        String name,
        String dataType,
        String unit,
        Double minValue,
        Double maxValue,
        String qualityRules
) {}
