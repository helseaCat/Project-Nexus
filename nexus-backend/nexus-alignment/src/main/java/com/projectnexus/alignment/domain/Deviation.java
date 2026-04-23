package com.projectnexus.alignment.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Records a detected deviation when a payload violates
 * a Data Contract rule or an Alignment Expectation.
 */
@Entity
@Table(name = "deviations")
@Getter
@Setter
@NoArgsConstructor
public class Deviation extends BaseTenantEntity {

    @Column(name = "payload_id", nullable = false)
    private UUID payloadId;

    @Column(name = "expectation_id")
    private UUID expectationId;

    @Column(name = "data_contract_id", nullable = false)
    private UUID dataContractId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "detected_value")
    private String detectedValue;

    @Column(name = "expected_value")
    private String expectedValue;
}
