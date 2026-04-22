package com.projectnexus.tasks.infrastructure;

import com.projectnexus.tasks.domain.LinkType;
import com.projectnexus.tasks.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByTenantId(UUID tenantId);

    List<Task> findByLinkedToTypeAndLinkedToId(LinkType linkType, UUID linkedToId);
}
