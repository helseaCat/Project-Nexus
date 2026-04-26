package com.projectnexus.alignment.application;

import com.projectnexus.alignment.application.dto.*;
import com.projectnexus.alignment.domain.AlignmentExpectation;
import com.projectnexus.alignment.domain.Severity;
import com.projectnexus.alignment.infrastructure.AlignmentExpectationRepository;
import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.application.DataContractService;
import com.projectnexus.contracts.application.dto.DataContractResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlignmentServiceImplTest {

    @Mock
    private AlignmentExpectationRepository repository;
    @Mock
    private DataContractService dataContractService;
    @InjectMocks
    private AlignmentServiceImpl service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID contractId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private DataContractResponse publishedContract() {
        return new DataContractResponse(contractId, tenantId, "Contract", null,
                "PUBLISHED", null, null, List.of(), 1, Instant.now(), Instant.now(), Instant.now(), null);
    }

    private AlignmentExpectation expectation(boolean active) {
        AlignmentExpectation e = new AlignmentExpectation();
        e.setId(UUID.randomUUID());
        e.setTenantId(tenantId);
        e.setDataContractId(contractId);
        e.setName("Pressure limit");
        e.setSeverity(Severity.CRITICAL);
        e.setRuleExpression("chamber_pressure < 115");
        e.setActive(active);
        return e;
    }

    @Test
    @DisplayName("Should create expectation against published contract")
    void shouldCreate() {
        when(dataContractService.getById(contractId)).thenReturn(publishedContract());
        when(repository.save(any())).thenAnswer(inv -> {
            AlignmentExpectation e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        var response = service.create(new AlignmentExpectationCreateRequest(
                contractId, "Pressure limit", "Max 115 bar", "CRITICAL", "chamber_pressure < 115"));

        assertNotNull(response.id());
        assertEquals("CRITICAL", response.severity());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Should reject expectation against draft contract")
    void shouldRejectDraftContract() {
        var draft = new DataContractResponse(contractId, tenantId, "Draft", null,
                "DRAFT", null, null, List.of(), 1, null, Instant.now(), Instant.now(), null);
        when(dataContractService.getById(contractId)).thenReturn(draft);

        assertThrows(IllegalStateException.class, () -> service.create(
                new AlignmentExpectationCreateRequest(contractId, "Test", null, "WARNING", "x > 0")));
    }

    @Test
    @DisplayName("Should reject invalid severity")
    void shouldRejectInvalidSeverity() {
        when(dataContractService.getById(contractId)).thenReturn(publishedContract());

        assertThrows(IllegalArgumentException.class, () -> service.create(
                new AlignmentExpectationCreateRequest(contractId, "Test", null, "INVALID", "x > 0")));
    }

    @Test
    @DisplayName("Should reject blank severity")
    void shouldRejectBlankSeverity() {
        when(dataContractService.getById(contractId)).thenReturn(publishedContract());

        assertThrows(IllegalArgumentException.class, () -> service.create(
                new AlignmentExpectationCreateRequest(contractId, "Test", null, "  ", "x > 0")));
    }

    @Test
    @DisplayName("Should deactivate and activate expectation")
    void shouldToggleActive() {
        AlignmentExpectation e = expectation(true);
        when(repository.findByIdAndTenantId(e.getId(), tenantId)).thenReturn(Optional.of(e));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(service.deactivate(e.getId()).active());

        e.setActive(false);
        assertTrue(service.activate(e.getId()).active());
    }

    @Test
    @DisplayName("Should throw not found for unknown ID")
    void shouldThrowNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(unknownId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(unknownId));
    }

    @Test
    @DisplayName("Should reject blank name in update")
    void shouldRejectBlankNameInUpdate() {
        AlignmentExpectation e = expectation(true);
        when(repository.findByIdAndTenantId(e.getId(), tenantId)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class, () ->
                service.update(e.getId(), new AlignmentExpectationUpdateRequest("  ", null, null, null)));
    }

    @Test
    @DisplayName("Should throw when no tenant context")
    void shouldThrowWithoutTenant() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, () -> service.create(
                new AlignmentExpectationCreateRequest(contractId, "Test", null, "WARNING", "x > 0")));
    }
}
