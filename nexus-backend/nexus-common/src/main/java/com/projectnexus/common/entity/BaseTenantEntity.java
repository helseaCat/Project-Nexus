package com.projectnexus.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base entity for all tenant-scoped entities in Project-Nexus.
 *
 * <p>Provides:
 * <ul>
 *   <li>UUID primary key generation</li>
 *   <li>Mandatory tenant isolation via {@code tenant_id} (used with PostgreSQL RLS)</li>
 *   <li>Spring Data JPA auditing ({@code createdBy}, {@code updatedBy}, {@code createdAt}, {@code updatedAt})</li>
 *   <li>Soft-delete support ({@code deleted}, {@code deletedAt})</li>
 *   <li>Hibernate tenant filter for query-level isolation</li>
 * </ul>
 *
 * <p>Every domain entity in the system <strong>must</strong> extend this class to ensure
 * consistent multi-tenancy and audit trail compliance.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Marks this entity as soft-deleted with the current timestamp.
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    /**
     * Restores a soft-deleted entity.
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }
}
