package com.projectnexus.ingestion.processing;

import com.projectnexus.alignment.domain.Deviation;
import com.projectnexus.ingestion.application.dto.Violation;

import java.util.List;
import java.util.UUID;

/**
 * Persists validation violations as Deviation entities.
 */
public interface DeviationRecorder {

    /**
     * Records each violation as a Deviation entity.
     *
     * @param payloadId  the payload that was validated
     * @param contractId the data contract the payload was submitted against
     * @param tenantId   the tenant owning the payload
     * @param violations the list of detected violations
     * @return the persisted Deviation entities
     */
    List<Deviation> record(UUID payloadId, UUID contractId, UUID tenantId, List<Violation> violations);
}
