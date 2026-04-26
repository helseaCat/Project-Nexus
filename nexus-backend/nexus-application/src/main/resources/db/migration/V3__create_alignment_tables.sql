-- =============================================================================
-- V3: Create Alignment Expectations, Payloads, and Deviations tables
-- =============================================================================
-- Core tables for the downstream-owned Goal Alignment & Monitoring context.
-- =============================================================================

CREATE TABLE alignment_expectations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID         NOT NULL,
    data_contract_id  UUID         NOT NULL REFERENCES data_contracts(id),
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    severity          VARCHAR(20)  NOT NULL DEFAULT 'WARNING',
    rule_expression   TEXT         NOT NULL,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by        UUID,
    updated_by        UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX idx_expectations_contract        ON alignment_expectations (data_contract_id);
CREATE INDEX idx_expectations_tenant_contract ON alignment_expectations (tenant_id, data_contract_id);

SELECT enable_rls_for_table('alignment_expectations');

-- =============================================================================

CREATE TABLE payloads (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID         NOT NULL,
    data_contract_id  UUID         NOT NULL REFERENCES data_contracts(id),
    raw_payload       JSONB,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    s3_key            VARCHAR(512),
    created_by        UUID,
    updated_by        UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX idx_payloads_tenant_contract ON payloads (tenant_id, data_contract_id);
CREATE INDEX idx_payloads_status          ON payloads (status);

SELECT enable_rls_for_table('payloads');

-- =============================================================================

CREATE TABLE deviations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID         NOT NULL,
    payload_id        UUID         NOT NULL REFERENCES payloads(id),
    expectation_id    UUID         REFERENCES alignment_expectations(id),
    data_contract_id  UUID         NOT NULL REFERENCES data_contracts(id),
    severity          VARCHAR(20)  NOT NULL,
    description       TEXT         NOT NULL,
    detected_value    VARCHAR(255),
    expected_value    VARCHAR(255),
    created_by        UUID,
    updated_by        UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX idx_deviations_payload     ON deviations (payload_id);
CREATE INDEX idx_deviations_expectation ON deviations (expectation_id);
CREATE INDEX idx_deviations_tenant      ON deviations (tenant_id);

SELECT enable_rls_for_table('deviations');
