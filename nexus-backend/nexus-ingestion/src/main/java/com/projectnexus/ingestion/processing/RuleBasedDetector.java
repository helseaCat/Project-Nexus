package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.Severity;
import com.projectnexus.ingestion.application.dto.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates rule expressions against payload data and produces violations
 * when the data breaks the defined constraint.
 *
 * <p>Supported expression formats:
 * <ul>
 *   <li>{@code field_name < threshold} – less than</li>
 *   <li>{@code field_name > threshold} – greater than</li>
 *   <li>{@code field_name BETWEEN min AND max} – range check</li>
 *   <li>{@code field_name == value} – equality</li>
 * </ul>
 *
 * <p>A violation is returned when the payload value BREAKS the rule constraint.
 */
@Slf4j
@Service
public class RuleBasedDetector {

    private static final Pattern LESS_THAN_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*<\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*>\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    private static final Pattern BETWEEN_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s+BETWEEN\\s+(-?\\d+(?:\\.\\d+)?)\\s+AND\\s+(-?\\d+(?:\\.\\d+)?)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EQUALITY_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*==\\s*(.+)\\s*$");

    /**
     * Evaluates a rule expression against the given payload.
     *
     * @param expression   the rule expression defining the constraint
     * @param payload      the data payload to evaluate
     * @param severity     the severity level if a violation is produced
     * @param expectationId the ID of the alignment expectation that owns this rule
     * @return a Violation if the data breaks the rule, or empty if the rule is satisfied or cannot be evaluated
     */
    public Optional<Violation> evaluateExpression(String expression, Map<String, Object> payload,
                                                  Severity severity, UUID expectationId) {
        if (expression == null || expression.isBlank()) {
            log.warn("Expression is null or blank, skipping evaluation");
            return Optional.empty();
        }

        Matcher betweenMatcher = BETWEEN_PATTERN.matcher(expression);
        if (betweenMatcher.matches()) {
            return evaluateBetween(betweenMatcher, payload, severity, expectationId, expression);
        }

        Matcher lessThanMatcher = LESS_THAN_PATTERN.matcher(expression);
        if (lessThanMatcher.matches()) {
            return evaluateLessThan(lessThanMatcher, payload, severity, expectationId, expression);
        }

        Matcher greaterThanMatcher = GREATER_THAN_PATTERN.matcher(expression);
        if (greaterThanMatcher.matches()) {
            return evaluateGreaterThan(greaterThanMatcher, payload, severity, expectationId, expression);
        }

        Matcher equalityMatcher = EQUALITY_PATTERN.matcher(expression);
        if (equalityMatcher.matches()) {
            return evaluateEquality(equalityMatcher, payload, severity, expectationId, expression);
        }

        log.warn("Malformed expression '{}', skipping evaluation", expression);
        return Optional.empty();
    }

    private Optional<Violation> evaluateLessThan(Matcher matcher, Map<String, Object> payload,
                                                 Severity severity, UUID expectationId, String expression) {
        String fieldName = matcher.group(1);
        double threshold = Double.parseDouble(matcher.group(2));

        Optional<Double> fieldValue = extractNumericValue(fieldName, payload, expression);
        if (fieldValue.isEmpty()) {
            return Optional.empty();
        }

        double value = fieldValue.get();
        // Rule: field < threshold. Violation when field >= threshold.
        if (value >= threshold) {
            return Optional.of(new Violation(
                    "Rule violated: '" + fieldName + "' must be < " + threshold + " but was " + value,
                    String.valueOf(value),
                    "< " + threshold,
                    severity,
                    expectationId
            ));
        }
        return Optional.empty();
    }

    private Optional<Violation> evaluateGreaterThan(Matcher matcher, Map<String, Object> payload,
                                                    Severity severity, UUID expectationId, String expression) {
        String fieldName = matcher.group(1);
        double threshold = Double.parseDouble(matcher.group(2));

        Optional<Double> fieldValue = extractNumericValue(fieldName, payload, expression);
        if (fieldValue.isEmpty()) {
            return Optional.empty();
        }

        double value = fieldValue.get();
        // Rule: field > threshold. Violation when field <= threshold.
        if (value <= threshold) {
            return Optional.of(new Violation(
                    "Rule violated: '" + fieldName + "' must be > " + threshold + " but was " + value,
                    String.valueOf(value),
                    "> " + threshold,
                    severity,
                    expectationId
            ));
        }
        return Optional.empty();
    }

    private Optional<Violation> evaluateBetween(Matcher matcher, Map<String, Object> payload,
                                                Severity severity, UUID expectationId, String expression) {
        String fieldName = matcher.group(1);
        double min = Double.parseDouble(matcher.group(2));
        double max = Double.parseDouble(matcher.group(3));

        if (min > max) {
            log.warn("Malformed BETWEEN expression '{}': min ({}) > max ({}), skipping", expression, min, max);
            return Optional.empty();
        }

        Optional<Double> fieldValue = extractNumericValue(fieldName, payload, expression);
        if (fieldValue.isEmpty()) {
            return Optional.empty();
        }

        double value = fieldValue.get();
        // Rule: field BETWEEN min AND max. Violation when field < min OR field > max.
        if (value < min || value > max) {
            String expectedRange = "[" + min + ", " + max + "]";
            return Optional.of(new Violation(
                    "Rule violated: '" + fieldName + "' must be between " + min + " and " + max + " but was " + value,
                    String.valueOf(value),
                    expectedRange,
                    severity,
                    expectationId
            ));
        }
        return Optional.empty();
    }

    private Optional<Violation> evaluateEquality(Matcher matcher, Map<String, Object> payload,
                                                 Severity severity, UUID expectationId, String expression) {
        String fieldName = matcher.group(1);
        String expectedValue = matcher.group(2).trim();

        if (!payload.containsKey(fieldName)) {
            log.warn("Field '{}' not found in payload for expression '{}', skipping", fieldName, expression);
            return Optional.empty();
        }

        Object actualValue = payload.get(fieldName);
        String actualStr = actualValue == null ? "null" : actualValue.toString();

        // Rule: field == value. Violation when field != value.
        if (!actualStr.equals(expectedValue)) {
            return Optional.of(new Violation(
                    "Rule violated: '" + fieldName + "' must be == " + expectedValue + " but was " + actualStr,
                    actualStr,
                    expectedValue,
                    severity,
                    expectationId
            ));
        }
        return Optional.empty();
    }

    private Optional<Double> extractNumericValue(String fieldName, Map<String, Object> payload, String expression) {
        if (!payload.containsKey(fieldName)) {
            log.warn("Field '{}' not found in payload for expression '{}', skipping", fieldName, expression);
            return Optional.empty();
        }

        Object rawValue = payload.get(fieldName);
        if (rawValue instanceof Number number) {
            return Optional.of(number.doubleValue());
        }

        log.warn("Field '{}' is not numeric (type: {}) for expression '{}', skipping",
                fieldName, rawValue == null ? "null" : rawValue.getClass().getSimpleName(), expression);
        return Optional.empty();
    }
}
