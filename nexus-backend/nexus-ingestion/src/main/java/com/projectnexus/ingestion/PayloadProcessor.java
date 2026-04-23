package com.projectnexus.ingestion;

/**
 * Async payload processor.
 * Consumes from RabbitMQ, validates against contract + expectations,
 * persists results, and triggers AI deviation detection.
 */
public interface PayloadProcessor {

    // TODO: Implement async processing pipeline
    // - processPayload(PayloadMessage message)
    //   1. Full validation against DataContract
    //   2. Evaluate all active AlignmentExpectations
    //   3. Persist Payload + Deviation records
    //   4. Trigger AI DeviationDetector
    //   5. If deviations → trigger AI TaskSuggester
    //   6. Send WebSocket notifications
}
