package com.projectnexus.alignment.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing Alignment Expectation.
 */
public record AlignmentExpectationUpdateRequest(

        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String name,

        String description,

        String severity,

        String ruleExpression
) {}
