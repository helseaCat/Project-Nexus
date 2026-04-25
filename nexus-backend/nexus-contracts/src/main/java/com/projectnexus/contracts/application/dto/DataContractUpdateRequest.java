package com.projectnexus.contracts.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for updating an existing (DRAFT) Data Contract.
 */
public record DataContractUpdateRequest(

        @Size(max = 255, message = "Contract name must be 255 characters or fewer")
        String name,

        String description,

        Object businessGoals,

        Object sharingRules,

        @Valid
        List<TestVariableRequest> testVariables
) {}
