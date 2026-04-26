package com.projectnexus.contracts.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating a new Data Contract.
 */
public record DataContractCreateRequest(

        @NotBlank(message = "Contract name is required")
        @Size(max = 255, message = "Contract name must be 255 characters or fewer")
        String name,

        String description,

        Object businessGoals,

        Object sharingRules,

        @Valid
        List<TestVariableRequest> testVariables
) {}
