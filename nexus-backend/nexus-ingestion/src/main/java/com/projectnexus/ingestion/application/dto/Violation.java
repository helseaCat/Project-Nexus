package com.projectnexus.ingestion.application.dto;

import com.projectnexus.alignment.domain.Severity;

import java.util.UUID;

/**
 * Internal record representing a single validation violation.
 * Used to communicate between validator/evaluator components and the DeviationRecorder.
 */
public record Violation(
        String description,
        String detectedValue,
        String expectedValue,
        Severity severity,
        UUID expectationId
) {}
