package com.projectnexus.common.tenant;

import java.util.UUID;

/**
 * Thread-local holder for the current tenant ID.
 *
 * <p>Set from JWT claims on every request by {@link TenantFilter}, and used by
 * {@link TenantHolder} to propagate the tenant ID to PostgreSQL RLS via
 * {@code SET app.current_tenant}.
 *
 * <p>Always cleared in a {@code finally} block to prevent tenant leakage
 * between requests on pooled threads.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {
        // utility class
    }

    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(UUID userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * Clears all thread-local state. Must be called in a finally block
     * after every request to prevent cross-request tenant leakage.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}
