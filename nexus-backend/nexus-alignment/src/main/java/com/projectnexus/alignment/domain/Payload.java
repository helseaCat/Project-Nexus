package com.projectnexus.alignment.domain;

import com.projectnexus.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Represents an ingested data payload submitted against a Data Contract.
 */
@Entity
@Table(name = "payloads")
@Getter
@Setter
@NoArgsConstructor
public class Payload extends BaseTenantEntity {

    @Column(name = "data_contract_id", nullable = false)
    private UUID dataContractId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Object rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayloadStatus status = PayloadStatus.PENDING;

    @Column(name = "s3_key")
    private String s3Key;
}
