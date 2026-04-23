package com.projectnexus.contracts.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines a single variable/field within a Data Contract's schema.
 * Includes type, unit, acceptable range, and quality rules.
 */
@Entity
@Table(name = "test_variables")
@Getter
@Setter
@NoArgsConstructor
public class TestVariable extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_contract_id", nullable = false)
    private DataContract dataContract;

    @Column(nullable = false)
    private String name;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    private String unit;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(columnDefinition = "TEXT")
    private String qualityRules;
}
