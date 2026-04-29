-- =============================================================================
-- V4: Create Tasks and Comments tables
-- =============================================================================
-- Core tables for the cross-team Tasks & Workflows bounded context.
-- Tasks can be linked to Data Contracts, Payloads, or Deviations.
-- =============================================================================

CREATE TABLE tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    assignee_id     UUID,
    due_date        TIMESTAMP,
    linked_to_type  VARCHAR(20),
    linked_to_id    UUID,
    ai_generated    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by      UUID,
    updated_by      UUID,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_tasks_tenant_status   ON tasks (tenant_id, status);
CREATE INDEX idx_tasks_tenant_assignee ON tasks (tenant_id, assignee_id);
CREATE INDEX idx_tasks_linked          ON tasks (linked_to_type, linked_to_id);

SELECT enable_rls_for_table('tasks');

-- =============================================================================

CREATE TABLE comments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    task_id     UUID         NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id   UUID         NOT NULL,
    body        TEXT         NOT NULL,
    created_by  UUID,
    updated_by  UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_comments_task        ON comments (task_id);
CREATE INDEX idx_comments_tenant_task ON comments (tenant_id, task_id);

SELECT enable_rls_for_table('comments');
