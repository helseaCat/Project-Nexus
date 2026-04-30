package com.projectnexus.tasks.application;

import com.projectnexus.tasks.application.dto.TaskCreateRequest;
import com.projectnexus.tasks.application.dto.TaskResponse;
import com.projectnexus.tasks.application.dto.TaskUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Application service for Task management and Kanban workflows.
 * All operations are tenant-scoped via TenantContext.
 */
public interface TaskService {

    TaskResponse create(TaskCreateRequest request);

    TaskResponse getById(UUID id);

    Page<TaskResponse> listForCurrentTenant(Pageable pageable);

    Page<TaskResponse> listByStatus(String status, Pageable pageable);

    TaskResponse update(UUID id, TaskUpdateRequest request);

    TaskResponse transitionStatus(UUID id, String newStatus);
}
