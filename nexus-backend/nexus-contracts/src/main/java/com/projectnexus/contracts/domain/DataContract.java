package com.projectnexus.contracts.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Core entity representing an upstream team's data product contract.
 *
 * <p>A Data Contract is the single source of truth for a data product. It defines:
 * <ul>
 *   <li>Business goals and context</li>
 *   <li>Test variables (schema with types, units, acceptable ranges)</li>
 *   <li>Sharing rules (which downstream teams can consume)</li>
 * </ul>
 *
 * <p>Contracts follow a DRAFT → PUBLISHED lifecycle. Once published, a contract
 * is immutable — modifications require creating a new version.
 */
@Entity
@Table(name = "data_contracts", indexes = {
        @Index(name = "idx_contracts_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_contracts_tenant_name", columnList = "tenant_id, name")
})
@Getter
@Setter
@NoArgsConstructor
public class DataContract extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractStatus status = ContractStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_goals", columnDefinition = "jsonb")
    private Object businessGoals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sharing_rules", columnDefinition = "jsonb")
    private Object sharingRules;

    @OneToMany(mappedBy = "dataContract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestVariable> testVariables = new ArrayList<>();

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "optimistic_lock_version")
    private Long optimisticLockVersion;

    /**
     * Publishes this contract, making it immutable and available for
     * downstream alignment expectations and payload validation.
     *
     * @throws IllegalStateException if the contract is already published
     */
    public void publish() {
        if (this.status == ContractStatus.PUBLISHED) {
            throw new IllegalStateException("Contract is already published");
        }
        this.status = ContractStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    /**
     * Adds a test variable to this contract and sets the bidirectional relationship.
     */
    public void addTestVariable(TestVariable variable) {
        testVariables.add(variable);
        variable.setDataContract(this);
    }

    /**
     * Removes a test variable from this contract.
     */
    public void removeTestVariable(TestVariable variable) {
        testVariables.remove(variable);
        variable.setDataContract(null);
    }
}
