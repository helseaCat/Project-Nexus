package com.projectnexus.ingestion.infrastructure;

import java.util.Optional;
import java.util.UUID;

/**
 * Uploads raw payload JSON to S3 for audit and reprocessing.
 * Returns the generated S3 key on success, or empty on failure (non-blocking).
 */
public interface S3StorageService {

    /**
     * Upload raw payload content to S3.
     *
     * @param tenantId   the tenant owning the payload
     * @param contractId the data contract the payload is submitted against
     * @param payloadId  the unique payload identifier
     * @param jsonContent the raw JSON string to store
     * @return the S3 key if upload succeeds, empty otherwise
     */
    Optional<String> upload(UUID tenantId, UUID contractId, UUID payloadId, String jsonContent);
}
