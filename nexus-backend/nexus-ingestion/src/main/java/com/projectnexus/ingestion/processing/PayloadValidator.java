package com.projectnexus.ingestion.processing;

import com.projectnexus.contracts.domain.TestVariable;
import com.projectnexus.ingestion.application.dto.Violation;

import java.util.List;
import java.util.Map;

/**
 * Validates raw payload data against Data Contract TestVariable definitions.
 *
 * <p>Checks performed:
 * <ul>
 *   <li>Type mismatch — field value doesn't match declared type</li>
 *   <li>Range violation — numeric value outside min/max bounds</li>
 *   <li>Missing field — TestVariable defined but absent from payload</li>
 * </ul>
 */
public interface PayloadValidator {

    /**
     * Validates the raw payload fields against the given test variable definitions.
     *
     * @param rawPayload    the submitted payload data as field name → value
     * @param testVariables the contract's declared test variable definitions
     * @return list of violations found; empty if payload conforms to contract
     */
    List<Violation> validate(Map<String, Object> rawPayload, List<TestVariable> testVariables);
}
