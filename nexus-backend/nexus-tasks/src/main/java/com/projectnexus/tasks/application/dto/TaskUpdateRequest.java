package com.projectnexus.tasks.application.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request payload for updating an existing Task.
 * All fields are optional — only non-null values are applied.
 */
public record TaskUpdateRequest(

        @Size(max = 255, message = "Title must be 255 characters or fewer")
        String title,

        String description,

        UUID assigneeId,

        LocalDateTime dueDate
) {}
