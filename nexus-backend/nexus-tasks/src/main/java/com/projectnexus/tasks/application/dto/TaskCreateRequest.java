package com.projectnexus.tasks.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request payload for creating a new Task.
 */
public record TaskCreateRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Title must be 255 characters or fewer")
        String title,

        String description,

        UUID assigneeId,

        LocalDateTime dueDate,

        String linkedToType,

        UUID linkedToId
) {}
