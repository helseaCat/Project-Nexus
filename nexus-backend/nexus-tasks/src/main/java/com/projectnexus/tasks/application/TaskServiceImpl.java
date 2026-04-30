package com.projectnexus.tasks.application;

import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.tasks.application.dto.*;
import com.projectnexus.tasks.domain.LinkType;
import com.projectnexus.tasks.domain.Task;
import com.projectnexus.tasks.domain.TaskStatus;
import com.projectnexus.tasks.infrastructure.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link TaskService}.
 * Handles task CRUD and Kanban status transitions with tenant isolation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;

    @Override
    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        UUID tenantId = requireTenantId();

        Task task = new Task();
        task.setTenantId(tenantId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssigneeId(request.assigneeId());
        task.setDueDate(request.dueDate());

        if (request.linkedToType() != null) {
            task.setLinkedToType(parseLinkType(request.linkedToType()));
            task.setLinkedToId(request.linkedToId());
        }

        Task saved = repository.save(task);
        log.info("Created Task id={} title='{}' for tenant={}", saved.getId(), saved.getTitle(), tenantId);
        return toResponse(saved);
    }

    @Override
    public TaskResponse getById(UUID id) {
        return toResponse(findByIdForTenant(id));
    }

    @Override
    public Page<TaskResponse> listForCurrentTenant(Pageable pageable) {
        return repository.findByTenantId(requireTenantId(), pageable).map(this::toResponse);
    }

    @Override
    public Page<TaskResponse> listByStatus(String status, Pageable pageable) {
        TaskStatus taskStatus = parseTaskStatus(status);
        return repository.findByTenantIdAndStatus(requireTenantId(), taskStatus, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, TaskUpdateRequest request) {
        Task task = findByIdForTenant(id);

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Task title cannot be blank");
            }
            task.setTitle(title);
        }
        if (request.description() != null) task.setDescription(request.description());
        if (request.assigneeId() != null) task.setAssigneeId(request.assigneeId());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());

        return toResponse(repository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse transitionStatus(UUID id, String newStatus) {
        Task task = findByIdForTenant(id);
        task.transitionTo(parseTaskStatus(newStatus));
        Task saved = repository.save(task);
        log.info("Task id={} transitioned to {}", saved.getId(), newStatus);
        return toResponse(saved);
    }

    private Task findByIdForTenant(UUID id) {
        return repository.findByIdAndTenantId(id, requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(), t.getTenantId(), t.getTitle(), t.getDescription(),
                t.getStatus().name(), t.getAssigneeId(), t.getDueDate(),
                t.getLinkedToType() != null ? t.getLinkedToType().name() : null,
                t.getLinkedToId(), t.isAiGenerated(),
                t.getCreatedAt(), t.getUpdatedAt(), t.getCreatedBy());
    }

    private TaskStatus parseTaskStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: '" + status + "'. Must be TODO, IN_PROGRESS, IN_REVIEW, or DONE.");
        }
    }

    private LinkType parseLinkType(String linkType) {
        try {
            return LinkType.valueOf(linkType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid link type: '" + linkType + "'. Must be CONTRACT, PAYLOAD, or DEVIATION.");
        }
    }

    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenantId;
    }
}
