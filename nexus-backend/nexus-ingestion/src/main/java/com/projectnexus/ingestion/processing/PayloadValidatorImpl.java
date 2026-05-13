package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.Severity;
import com.projectnexus.contracts.domain.TestVariable;
import com.projectnexus.ingestion.application.dto.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Validates payload fields against Data Contract TestVariable definitions.
 *
 * <p>All violations produced by this validator use {@link Severity#CRITICAL}
 * and a null expectationId, since they represent contract-level violations
 * (not alignment expectation violations).
 */
@Slf4j
@Service
public class PayloadValidatorImpl implements PayloadValidator {

    @Override
    public List<Violation> validate(Map<String, Object> rawPayload, List<TestVariable> testVariables) {
        if (testVariables == null) {
            return List.of();
        }
        Map<String, Object> payload = rawPayload == null ? Collections.emptyMap() : rawPayload;
        List<Violation> violations = new ArrayList<>();

        for (TestVariable variable : testVariables) {
            if (variable == null || variable.getName() == null) {
                continue;
            }
            String fieldName = variable.getName();

            if (!payload.containsKey(fieldName)) {
                violations.add(missingFieldViolation(fieldName));
                continue;
            }

            Object value = payload.get(fieldName);
            String declaredType = variable.getDataType();

            if (!isTypeMatch(value, declaredType)) {
                violations.add(typeMismatchViolation(fieldName, value, declaredType));
                continue;
            }

            if (isNumericType(declaredType) && value instanceof Number numericValue) {
                checkRange(violations, fieldName, numericValue, variable);
            }
        }

        return violations;
    }

    private boolean isTypeMatch(Object value, String declaredType) {
        if (value == null) {
            return false;
        }
        if (declaredType == null || declaredType.isBlank()) {
            log.warn("Declared type is null or blank, failing type check");
            return false;
        }
        return switch (declaredType.toUpperCase(Locale.ROOT)) {
            case "DOUBLE" -> value instanceof Number;
            case "INTEGER" -> value instanceof Integer || value instanceof Long;
            case "STRING" -> value instanceof String;
            case "BOOLEAN" -> value instanceof Boolean;
            default -> {
                log.warn("Unknown declared type '{}', failing type check", declaredType);
                yield false;
            }
        };
    }

    private boolean isNumericType(String declaredType) {
        return "DOUBLE".equalsIgnoreCase(declaredType) || "INTEGER".equalsIgnoreCase(declaredType);
    }

    private void checkRange(List<Violation> violations, String fieldName, Number value, TestVariable variable) {
        Double minValue = variable.getMinValue();
        Double maxValue = variable.getMaxValue();

        if (minValue == null && maxValue == null) {
            return;
        }

        double numericValue = value.doubleValue();

        if (minValue != null && numericValue < minValue) {
            violations.add(rangeViolation(fieldName, numericValue, minValue, maxValue));
        } else if (maxValue != null && numericValue > maxValue) {
            violations.add(rangeViolation(fieldName, numericValue, minValue, maxValue));
        }
    }

    private Violation missingFieldViolation(String fieldName) {
        return new Violation(
                "Missing required field: " + fieldName,
                "absent",
                "field '" + fieldName + "' must be present",
                Severity.CRITICAL,
                null
        );
    }

    private Violation typeMismatchViolation(String fieldName, Object value, String expectedType) {
        String actualType = value == null ? "null" : value.getClass().getSimpleName();
        return new Violation(
                "Type mismatch for field '" + fieldName + "': expected " + expectedType + " but got " + actualType,
                actualType,
                expectedType,
                Severity.CRITICAL,
                null
        );
    }

    private Violation rangeViolation(String fieldName, double detectedValue, Double min, Double max) {
        String expectedRange = "[" + (min != null ? min : "-∞") + ", " + (max != null ? max : "∞") + "]";
        return new Violation(
                "Range violation for field '" + fieldName + "': value " + detectedValue + " is outside " + expectedRange,
                String.valueOf(detectedValue),
                expectedRange,
                Severity.CRITICAL,
                null
        );
    }
}
