package com.projectnexus.contracts.api;

import java.util.UUID;

/**
 * Lightweight contract representation exposed to other bounded contexts.
 * Other modules depend on this API package — never on domain internals.
 */
public record ContractSummary(
        UUID id,
        UUID tenantId,
        String name,
        String status,
        int version
) {}
