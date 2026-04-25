package com.projectnexus.contracts.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines a single variable/field within a Data Contract's schema.
 *
 * <p>Each test variable specifies:
 * <ul>
 *   <li>Name and data type (e.g., "chamber_pressure", "DOUBLE")</li>
 *   <li>Unit of measurement (e.g., "bar", "N", "°C")</li>
 *   <li>Acceptable range ({@code minValue} to {@code maxValue})</li>
 *   <li>Quality rules (free-text validation criteria)</li>
 * </ul>
 */
@Entity
@Table(name = "test_variables", indexes = {
        @Index(name = "idx_testvars_contract", columnList = "data_contract_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TestVariable extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_contract_id", nullable = false)
    private DataContract dataContract;

    @Column(nullable = false)
    private String name;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(length = 50)
    private String unit;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "quality_rules", columnDefinition = "TEXT")
    private String qualityRules;
}
