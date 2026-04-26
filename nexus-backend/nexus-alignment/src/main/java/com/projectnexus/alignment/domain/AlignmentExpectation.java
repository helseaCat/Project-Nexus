package com.projectnexus.alignment.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Downstream-defined expectation against a published Data Contract.
 *
 * <p>Downstream teams create expectations to monitor upstream data against
 * their own safety or quality thresholds. Every incoming payload is evaluated
 * against all active expectations for the linked contract.
 */
@Entity
@Table(name = "alignment_expectations", indexes = {
        @Index(name = "idx_expectations_contract", columnList = "data_contract_id"),
        @Index(name = "idx_expectations_tenant_contract", columnList = "tenant_id, data_contract_id")
})
@Getter
@Setter
@NoArgsConstructor
public class AlignmentExpectation extends BaseTenantEntity {

    @Column(name = "data_contract_id", nullable = false)
    private UUID dataContractId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity = Severity.WARNING;

    @Column(name = "rule_expression", columnDefinition = "TEXT", nullable = false)
    private String ruleExpression;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Deactivates this expectation so it is no longer evaluated on incoming payloads.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactivates a previously deactivated expectation.
     */
    public void activate() {
        this.active = true;
    }
}
