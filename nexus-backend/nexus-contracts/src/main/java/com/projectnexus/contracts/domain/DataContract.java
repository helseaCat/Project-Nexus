package com.projectnexus.contracts.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Core entity representing an upstream team's data product contract.
 * Defines schema, business goals, validation rules, and sharing policies.
 */
@Entity
@Table(name = "data_contracts")
@Getter
@Setter
@NoArgsConstructor
public class DataContract extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_goals", columnDefinition = "jsonb")
    private Object businessGoals;

    @OneToMany(mappedBy = "dataContract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestVariable> testVariables = new ArrayList<>();

    @Column(nullable = false)
    private int version = 1;
}
