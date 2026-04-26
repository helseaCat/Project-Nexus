package com.projectnexus.alignment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for creating a new Alignment Expectation.
 */
public record AlignmentExpectationCreateRequest(

        @NotNull(message = "Data contract ID is required")
        UUID dataContractId,

        @NotBlank(message = "Expectation name is required")
        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String name,

        String description,

        @NotBlank(message = "Severity is required (WARNING or CRITICAL)")
        String severity,

        @NotBlank(message = "Rule expression is required")
        String ruleExpression
) {}
