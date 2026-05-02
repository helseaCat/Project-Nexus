package com.projectnexus.tasks.application.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload for a Task.
 */
public record TaskResponse(
        UUID id,
        UUID tenantId,
        String title,
        String description,
        String status,
        UUID assigneeId,
        LocalDateTime dueDate,
        String linkedToType,
        UUID linkedToId,
        boolean aiGenerated,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {}
