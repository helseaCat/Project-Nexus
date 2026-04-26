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
        TenantContext.setCurrentUser(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private DataContractResponse publishedContract() {
        return new DataContractResponse(contractId, tenantId, "Test Contract", null, "PUBLISHED",
                null, null, List.of(), 1, Instant.now(), Instant.now(), Instant.now(), null);
    }

    private DataContractResponse draftContract() {
        return new DataContractResponse(contractId, tenantId, "Draft Contract", null, "DRAFT",
                null, null, List.of(), 1, null, Instant.now(), Instant.now(), null);
    }

    @Test
    @DisplayName("Should create an expectation against a published contract")
    void shouldCreateExpectation() {
        when(dataContractService.getById(contractId)).thenReturn(publishedContract());
        when(repository.save(any(AlignmentExpectation.class))).thenAnswer(inv -> {
            AlignmentExpectation e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        AlignmentExpectationCreateRequest request = new AlignmentExpectationCreateRequest(
                contractId, "Pressure limit", "Max 115 bar", "CRITICAL", "chamber_pressure < 115");

        AlignmentExpectationResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("Pressure limit", response.name());
        assertEquals("CRITICAL", response.severity());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Should reject expectation against a draft contract")
    void shouldRejectExpectationAgainstDraft() {
        when(dataContractService.getById(contractId)).thenReturn(draftContract());

        AlignmentExpectationCreateRequest request = new AlignmentExpectationCreateRequest(
                contractId, "Test", null, "WARNING", "x > 0");

        assertThrows(IllegalStateException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject invalid severity")
    void shouldRejectInvalidSeverity() {
        when(dataContractService.getById(contractId)).thenReturn(publishedContract());

        AlignmentExpectationCreateRequest request = new AlignmentExpectationCreateRequest(
                contractId, "Test", null, "INVALID", "x > 0");

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("Should deactivate an expectation")
    void shouldDeactivateExpectation() {
        AlignmentExpectation expectation = new AlignmentExpectation();
        expectation.setId(UUID.randomUUID());
        expectation.setTenantId(tenantId);
        expectation.setName("Test");
        expectation.setSeverity(Severity.WARNING);
        expectation.setActive(true);

        when(repository.findByIdAndTenantId(expectation.getId(), tenantId)).thenReturn(Optional.of(expectation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AlignmentExpectationResponse response = service.deactivate(expectation.getId());

        assertFalse(response.active());
    }

    @Test
    @DisplayName("Should activate a deactivated expectation")
    void shouldActivateExpectation() {
        AlignmentExpectation expectation = new AlignmentExpectation();
        expectation.setId(UUID.randomUUID());
        expectation.setTenantId(tenantId);
        expectation.setName("Test");
        expectation.setSeverity(Severity.WARNING);
        expectation.setActive(false);

        when(repository.findByIdAndTenantId(expectation.getId(), tenantId)).thenReturn(Optional.of(expectation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AlignmentExpectationResponse response = service.activate(expectation.getId());

        assertTrue(response.active());
    }

    @Test
    @DisplayName("Should throw not found for unknown expectation")
    void shouldThrowNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(unknownId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(unknownId));
    }

    @Test
    @DisplayName("Should update an expectation")
    void shouldUpdateExpectation() {
        AlignmentExpectation expectation = new AlignmentExpectation();
        expectation.setId(UUID.randomUUID());
        expectation.setTenantId(tenantId);
        expectation.setName("Original");
        expectation.setDescription("Old desc");
        expectation.setSeverity(Severity.WARNING);
        expectation.setRuleExpression("x > 0");

        when(repository.findByIdAndTenantId(expectation.getId(), tenantId)).thenReturn(Optional.of(expectation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AlignmentExpectationUpdateRequest request = new AlignmentExpectationUpdateRequest(
                "Updated", "New desc", "CRITICAL", "x > 10");

        AlignmentExpectationResponse response = service.update(expectation.getId(), request);

        assertEquals("Updated", response.name());
        assertEquals("CRITICAL", response.severity());
        assertEquals("x > 10", response.ruleExpression());
    }

    @Test
    @DisplayName("Should throw when no tenant context")
    void shouldThrowWhenNoTenantContext() {
        TenantContext.clear();
        AlignmentExpectationCreateRequest request = new AlignmentExpectationCreateRequest(
                contractId, "Test", null, "WARNING", "x > 0");
        assertThrows(IllegalStateException.class, () -> service.create(request));
    }
}
