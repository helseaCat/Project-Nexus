package com.projectnexus.tasks.infrastructure;

import com.projectnexus.tasks.domain.LinkType;
import com.projectnexus.tasks.domain.Task;
import com.projectnexus.tasks.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Task} persistence operations.
 * All queries are tenant-scoped for application-level isolation.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Task> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Task> findByTenantIdAndStatus(UUID tenantId, TaskStatus status, Pageable pageable);

    Page<Task> findByTenantIdAndAssigneeId(UUID tenantId, UUID assigneeId, Pageable pageable);

    List<Task> findByLinkedToTypeAndLinkedToId(LinkType linkType, UUID linkedToId);
}
