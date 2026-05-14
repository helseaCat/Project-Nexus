package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.AlignmentExpectation;
import com.projectnexus.ingestion.application.dto.Violation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Evaluates alignment expectations against raw payload data using rule-based detection.
 *
 * <p>Iterates all active expectations, delegating each rule expression to the
 * {@link RuleBasedDetector}. Inactive expectations are skipped.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpectationEvaluatorImpl implements ExpectationEvaluator {

    private final RuleBasedDetector ruleBasedDetector;

    @Override
    public List<Violation> evaluate(Map<String, Object> rawPayload, List<AlignmentExpectation> expectations) {
        if (expectations == null || expectations.isEmpty()) {
            return List.of();
        }

        List<Violation> violations = new ArrayList<>();

        for (AlignmentExpectation expectation : expectations) {
            if (!expectation.isActive()) {
                log.debug("Skipping inactive expectation: {}", expectation.getName());
                continue;
            }

            Optional<Violation> result = ruleBasedDetector.evaluateExpression(
                    expectation.getRuleExpression(),
                    rawPayload,
                    expectation.getSeverity(),
                    expectation.getId()
            );

            result.ifPresent(violations::add);
        }

        return violations;
    }
}
