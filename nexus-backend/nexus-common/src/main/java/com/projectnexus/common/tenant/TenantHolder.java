package com.projectnexus.common.tenant;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Propagates the current tenant ID to PostgreSQL session variables for Row Level Security (RLS).
 *
 * <p>PostgreSQL RLS policies reference {@code current_setting('app.current_tenant')} to enforce
 * tenant isolation at the database level. This class sets that session variable and enables
 * the Hibernate tenant filter on every request.
 *
 * <p>Called by {@link TenantFilter} after extracting the tenant ID from the JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantHolder {

    private final EntityManager entityManager;

    /**
     * Sets the PostgreSQL session variable {@code app.current_tenant} and enables
     * the Hibernate {@code tenantFilter} for the current persistence context.
     *
     * @param tenantId the tenant UUID extracted from the JWT
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void setTenantForRequest(UUID tenantId) {
        if (tenantId == null) {
            log.warn("Attempted to set null tenant ID — skipping RLS configuration");
            return;
        }

        // Set PostgreSQL session variable for RLS policies
        entityManager.createNativeQuery("SET LOCAL app.current_tenant = :tenantId")
                .setParameter("tenantId", tenantId.toString())
                .executeUpdate();

        // Enable Hibernate filter for query-level tenant isolation
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);

        log.debug("Tenant context set for RLS: tenantId={}", tenantId);
    }
}
