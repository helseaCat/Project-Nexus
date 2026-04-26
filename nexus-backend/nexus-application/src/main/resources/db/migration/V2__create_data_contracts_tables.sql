-- =============================================================================
-- V2: Create Data Contracts and Test Variables tables
-- =============================================================================
-- Core tables for the upstream-owned Data Contracts bounded context.
-- Both tables include tenant_id for RLS and full audit trail columns
-- inherited from BaseTenantEntity.
-- =============================================================================

CREATE TABLE data_contracts (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID        NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    business_goals          JSONB,
    sharing_rules           JSONB,
    version                 INT          NOT NULL DEFAULT 1,
    published_at            TIMESTAMPTZ,
    optimistic_lock_version BIGINT       NOT NULL DEFAULT 0,
    created_by              UUID,
    updated_by              UUID,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ,
    deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMPTZ
);

CREATE INDEX idx_contracts_tenant_status ON data_contracts (tenant_id, status);
CREATE INDEX idx_contracts_tenant_name   ON data_contracts (tenant_id, name);

-- Enable RLS for tenant isolation
SELECT enable_rls_for_table('data_contracts');

-- =============================================================================

CREATE TABLE test_variables (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID         NOT NULL,
    data_contract_id  UUID         NOT NULL REFERENCES data_contracts(id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    data_type         VARCHAR(50)  NOT NULL,
    unit              VARCHAR(50),
    min_value         DOUBLE PRECISION,
    max_value         DOUBLE PRECISION,
    quality_rules     TEXT,
    created_by        UUID,
    updated_by        UUID,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX idx_testvars_contract ON test_variables (data_contract_id);

-- Enable RLS for tenant isolation
SELECT enable_rls_for_table('test_variables');
