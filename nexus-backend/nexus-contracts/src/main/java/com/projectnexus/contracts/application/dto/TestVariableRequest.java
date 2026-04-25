package com.projectnexus.contracts.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a test variable within a Data Contract.
 */
public record TestVariableRequest(

        @NotBlank(message = "Variable name is required")
        @Size(max = 255, message = "Variable name must be 255 characters or fewer")
        String name,

        @NotBlank(message = "Data type is required")
        @Size(max = 50, message = "Data type must be 50 characters or fewer")
        String dataType,

        @Size(max = 50, message = "Unit must be 50 characters or fewer")
        String unit,

        Double minValue,

        Double maxValue,

        String qualityRules
) {}
