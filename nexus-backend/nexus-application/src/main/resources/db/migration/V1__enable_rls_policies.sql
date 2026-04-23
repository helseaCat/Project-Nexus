-- =============================================================================
-- V1: Enable Row Level Security (RLS) for multi-tenant isolation
-- =============================================================================
-- Every table with a tenant_id column must have RLS enabled.
-- Policies enforce that queries only see rows matching the current tenant,
-- set via: SET LOCAL app.current_tenant = '<tenant-uuid>';
--
-- This migration creates a reusable function and applies RLS to all
-- initial tables. Future migrations adding new tables MUST also call
-- enable_rls_for_table() or manually add equivalent policies.
-- =============================================================================

-- Set a default value for the session variable so current_setting() doesn't
-- throw an error when no tenant context is set (e.g., during migrations).
SELECT set_config('app.current_tenant', '00000000-0000-0000-0000-000000000000', false);

-- Helper function: enables RLS on a table with standard tenant isolation policy.
-- Usage: SELECT enable_rls_for_table('my_table_name');
CREATE OR REPLACE FUNCTION enable_rls_for_table(table_name TEXT)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', table_name);
    EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', table_name);

    -- Policy: rows are visible only when tenant_id matches the session variable
    EXECUTE format(
        'CREATE POLICY tenant_isolation_policy ON %I
         USING (tenant_id::text = current_setting(''app.current_tenant'', true))
         WITH CHECK (tenant_id::text = current_setting(''app.current_tenant'', true))',
        table_name
    );
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- RLS will be applied to each domain table as they are created in subsequent
-- migrations. Example:
--
--   CREATE TABLE data_contracts ( ... tenant_id UUID NOT NULL ... );
--   SELECT enable_rls_for_table('data_contracts');
--
-- The helper function above ensures consistent policy naming and behavior.
-- =============================================================================
