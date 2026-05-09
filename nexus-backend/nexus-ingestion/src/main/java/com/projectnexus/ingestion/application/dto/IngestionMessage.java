package com.projectnexus.ingestion.application.dto;

import java.util.UUID;

/**
 * Message published to RabbitMQ when a payload is submitted.
 * Consumed by the IngestionProcessor for async validation.
 */
public record IngestionMessage(
        UUID payloadId,
        UUID tenantId
) {}
