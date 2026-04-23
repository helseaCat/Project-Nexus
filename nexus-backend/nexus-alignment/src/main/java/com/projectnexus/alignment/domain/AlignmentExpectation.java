package com.projectnexus.alignment.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Downstream-defined expectation against a published Data Contract.
 * Monitored on every payload ingestion for deviation detection.
 */
@Entity
@Table(name = "alignment_expectations")
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
    @Column(nullable = false)
    private Severity severity = Severity.WARNING;

    @Column(name = "rule_expression", columnDefinition = "TEXT", nullable = false)
    private String ruleExpression;

    @Column(nullable = false)
    private boolean active = true;
}
