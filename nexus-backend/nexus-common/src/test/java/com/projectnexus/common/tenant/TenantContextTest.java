package com.projectnexus.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TenantContext} thread-local management.
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should store and retrieve tenant ID")
    void shouldStoreAndRetrieveTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);

        assertEquals(tenantId, TenantContext.getCurrentTenant());
    }

    @Test
    @DisplayName("Should store and retrieve user ID")
    void shouldStoreAndRetrieveUserId() {
        UUID userId = UUID.randomUUID();
        TenantContext.setCurrentUser(userId);

        assertEquals(userId, TenantContext.getCurrentUser());
    }

    @Test
    @DisplayName("Should return null when no tenant is set")
    void shouldReturnNullWhenNoTenantSet() {
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    @DisplayName("Should return null when no user is set")
    void shouldReturnNullWhenNoUserSet() {
        assertNull(TenantContext.getCurrentUser());
    }

    @Test
    @DisplayName("Should clear both tenant and user context")
    void shouldClearBothContexts() {
        TenantContext.setCurrentTenant(UUID.randomUUID());
        TenantContext.setCurrentUser(UUID.randomUUID());

        TenantContext.clear();

        assertNull(TenantContext.getCurrentTenant());
        assertNull(TenantContext.getCurrentUser());
    }

    @Test
    @DisplayName("Should isolate context between threads")
    void shouldIsolateContextBetweenThreads() throws InterruptedException {
        UUID mainTenant = UUID.randomUUID();
        TenantContext.setCurrentTenant(mainTenant);

        UUID[] childTenant = new UUID[1];
        Thread child = new Thread(() -> {
            childTenant[0] = TenantContext.getCurrentTenant();
        });
        child.start();
        child.join();

        assertEquals(mainTenant, TenantContext.getCurrentTenant());
        assertNull(childTenant[0], "Child thread should not see parent's tenant context");
    }
}
