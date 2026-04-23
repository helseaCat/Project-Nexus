package com.projectnexus.tasks.interfaces;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Task and Kanban board operations.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    // TODO: Inject TaskService
    // - POST   /              → createTask
    // - GET    /              → listTasks
    // - GET    /{id}          → getTask
    // - PATCH  /{id}/status   → updateStatus
    // - POST   /{id}/comments → addComment
}
