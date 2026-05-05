package com.projectnexus.ingestion.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to submit a data payload against a published Data Contract.
 */
public record PayloadSubmitRequest(
        @NotNull(message = "Data contract ID is required")
        UUID dataContractId,

        @NotNull(message = "Raw payload data is required")
        Object rawPayload
) {}
