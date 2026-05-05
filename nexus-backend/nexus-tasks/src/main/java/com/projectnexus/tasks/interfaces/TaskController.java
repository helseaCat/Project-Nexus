package com.projectnexus.tasks.interfaces;

import com.projectnexus.tasks.application.TaskService;
import com.projectnexus.tasks.application.dto.TaskCreateRequest;
import com.projectnexus.tasks.application.dto.TaskResponse;
import com.projectnexus.tasks.application.dto.TaskUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Task and Kanban board operations.
 *
 * <p><strong>Authorization (Phase 2 — not yet enforced):</strong>
 * All task endpoints will require authenticated tenant membership.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Cross-team task management with Kanban-style status transitions")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task, optionally linked to a contract, payload, or deviation")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all tasks for current tenant")
    public ResponseEntity<Page<TaskResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.listForCurrentTenant(pageable));
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "List tasks filtered by status")
    public ResponseEntity<Page<TaskResponse>> listByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.listByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates title, description, assignee, or due date")
    public ResponseEntity<TaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition task status", description = "Moves a task through the Kanban workflow (TODO → IN_PROGRESS → IN_REVIEW → DONE)")
    public ResponseEntity<TaskResponse> transitionStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(taskService.transitionStatus(id, status));
    }
}
