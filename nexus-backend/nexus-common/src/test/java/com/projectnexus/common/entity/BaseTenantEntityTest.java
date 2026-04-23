package com.projectnexus.common.entity;

import com.projectnexus.common.tenant.TenantContext;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BaseTenantEntity}.
 */
class BaseTenantEntityTest {

    /**
     * Concrete subclass for testing the abstract base entity.
     */
    @Entity
    @Table(name = "test_entity")
    static class TestEntity extends BaseTenantEntity {
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should auto-set tenantId from TenantContext on prePersist")
    void shouldAutoSetTenantIdFromContext() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);

        TestEntity entity = new TestEntity();
        assertNull(entity.getTenantId());

        // Simulate JPA @PrePersist callback
        entity.onPrePersist();

        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    @DisplayName("Should not overwrite explicitly set tenantId on prePersist")
    void shouldNotOverwriteExplicitTenantId() {
        UUID explicitTenant = UUID.randomUUID();
        UUID contextTenant = UUID.randomUUID();

        TenantContext.setCurrentTenant(contextTenant);

        TestEntity entity = new TestEntity();
        entity.setTenantId(explicitTenant);
        entity.onPrePersist();

        assertEquals(explicitTenant, entity.getTenantId(),
                "Explicitly set tenantId should not be overwritten by TenantContext");
    }

    @Test
    @DisplayName("Should leave tenantId null when no context is available")
    void shouldLeaveNullWhenNoContext() {
        TestEntity entity = new TestEntity();
        entity.onPrePersist();

        assertNull(entity.getTenantId());
    }

    @Test
    @DisplayName("Should soft-delete and restore correctly")
    void shouldSoftDeleteAndRestore() {
        TestEntity entity = new TestEntity();

        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());

        entity.softDelete();
        assertTrue(entity.isDeleted());
        assertNotNull(entity.getDeletedAt());

        entity.restore();
        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());
    }

    @Test
    @DisplayName("Should default deleted to false")
    void shouldDefaultDeletedToFalse() {
        TestEntity entity = new TestEntity();
        assertFalse(entity.isDeleted());
    }
}
