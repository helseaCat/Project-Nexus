package com.projectnexus.tasks.application;

import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.tasks.application.dto.TaskCreateRequest;
import com.projectnexus.tasks.domain.Task;
import com.projectnexus.tasks.domain.TaskStatus;
import com.projectnexus.tasks.infrastructure.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;
    @InjectMocks
    private TaskServiceImpl service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() { TenantContext.setCurrentTenant(tenantId); }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private Task task(TaskStatus status) {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setTenantId(tenantId);
        t.setTitle("Investigate pressure anomaly");
        if (status != TaskStatus.TODO) {
            t.transitionTo(status);
        }
        return t;
    }

    @Test
    @DisplayName("Should create a task with linked entity")
    void shouldCreate() {
        when(repository.save(any())).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        var response = service.create(new TaskCreateRequest(
                "Review deviation", "Check payload", null, null, "DEVIATION", UUID.randomUUID()));

        assertNotNull(response.id());
        assertEquals("TODO", response.status());
        assertEquals("DEVIATION", response.linkedToType());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
    }

    @Test
    @DisplayName("Should reject mismatched linked pair")
    void shouldRejectMismatchedLink() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new TaskCreateRequest("Test", null, null, null, "CONTRACT", null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should transition status via domain method")
    void shouldTransitionStatus() {
        Task t = task(TaskStatus.TODO);
        when(repository.findByIdAndTenantId(t.getId(), tenantId)).thenReturn(Optional.of(t));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.transitionStatus(t.getId(), "IN_PROGRESS");
        assertEquals("IN_PROGRESS", response.status());
    }

    @Test
    @DisplayName("Should reject blank title in create")
    void shouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new TaskCreateRequest("  ", null, null, null, null, null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw not found for unknown task")
    void shouldThrowNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(unknownId, tenantId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(unknownId));
    }

    @Test
    @DisplayName("Should throw without tenant context")
    void shouldThrowWithoutTenant() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, () -> service.create(
                new TaskCreateRequest("Test", null, null, null, null, null)));
        verify(repository, never()).save(any());
    }
}
