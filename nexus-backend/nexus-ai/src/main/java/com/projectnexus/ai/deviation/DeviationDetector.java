package com.projectnexus.ai.deviation;

/**
 * AI-powered deviation detector.
 * Receives rich context (contract + expectations + payload + history)
 * and identifies deviations that rule-based checks might miss.
 */
public interface DeviationDetector {

    // TODO: Implement with Spring AI
    // - analyzePayload(DeviationContext context) → List<DetectedDeviation>
}
