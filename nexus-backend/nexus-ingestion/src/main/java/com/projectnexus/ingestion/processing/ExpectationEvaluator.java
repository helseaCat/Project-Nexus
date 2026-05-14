package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.AlignmentExpectation;
import com.projectnexus.ingestion.application.dto.Violation;

import java.util.List;
import java.util.Map;

/**
 * Evaluates raw payload data against a set of alignment expectations.
 *
 * <p>Each active expectation's rule expression is evaluated against the payload.
 * Violations are returned with the expectation's configured severity.
 */
public interface ExpectationEvaluator {

    /**
     * Evaluates the raw payload against the given alignment expectations.
     *
     * @param rawPayload   the submitted payload data as field name → value
     * @param expectations the alignment expectations to evaluate against
     * @return list of violations found; empty if payload satisfies all expectations
     */
    List<Violation> evaluate(Map<String, Object> rawPayload, List<AlignmentExpectation> expectations);
}
