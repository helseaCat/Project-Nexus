package com.projectnexus.contracts.application;

import com.projectnexus.common.exception.ResourceNotFoundException;
import com.projectnexus.common.tenant.TenantContext;
import com.projectnexus.contracts.application.dto.*;
import com.projectnexus.contracts.domain.ContractStatus;
import com.projectnexus.contracts.domain.DataContract;
import com.projectnexus.contracts.infrastructure.DataContractRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DataContractServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class DataContractServiceImplTest {

    @Mock
    private DataContractRepository repository;

    @InjectMocks
    private DataContractServiceImpl service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        TenantContext.setCurrentUser(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create a contract with test variables")
    void shouldCreateContract() {
        DataContractCreateRequest request = new DataContractCreateRequest(
                "Booster Pressure Data",
                "Chamber pressure readings",
                null,
                null,
                List.of(new TestVariableRequest("chamber_pressure", "DOUBLE", "bar", 0.0, 200.0, null))
        );

        when(repository.save(any(DataContract.class))).thenAnswer(invocation -> {
            DataContract dc = invocation.getArgument(0);
            dc.setId(UUID.randomUUID());
            return dc;
        });

        DataContractResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("Booster Pressure Data", response.name());
        assertEquals("DRAFT", response.status());
        assertEquals(1, response.version());
        assertEquals(1, response.testVariables().size());
        assertEquals("chamber_pressure", response.testVariables().get(0).name());

        ArgumentCaptor<DataContract> captor = ArgumentCaptor.forClass(DataContract.class);
        verify(repository).save(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
    }

    @Test
    @DisplayName("Should reject test variable with minValue > maxValue")
    void shouldRejectInvalidRange() {
        DataContractCreateRequest request = new DataContractCreateRequest(
                "Bad Contract",
                null,
                null,
                null,
                List.of(new TestVariableRequest("temp", "DOUBLE", "°C", 100.0, 50.0, null))
        );

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should publish a draft contract")
    void shouldPublishContract() {
        DataContract contract = new DataContract();
        contract.setId(UUID.randomUUID());
        contract.setTenantId(tenantId);
        contract.setName("Test Contract");
        contract.setStatus(ContractStatus.DRAFT);

        when(repository.findByIdAndTenantId(contract.getId(), tenantId)).thenReturn(Optional.of(contract));
        when(repository.save(any(DataContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DataContractResponse response = service.publish(contract.getId());

        assertEquals("PUBLISHED", response.status());
        assertNotNull(response.publishedAt());
    }

    @Test
    @DisplayName("Should reject publishing an already published contract")
    void shouldRejectDoublePublish() {
        DataContract contract = new DataContract();
        contract.setId(UUID.randomUUID());
        contract.setTenantId(tenantId);
        contract.setName("Published Contract");
        contract.setStatus(ContractStatus.PUBLISHED);

        when(repository.findByIdAndTenantId(contract.getId(), tenantId)).thenReturn(Optional.of(contract));

        assertThrows(IllegalStateException.class, () -> service.publish(contract.getId()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject update on a published contract")
    void shouldRejectUpdateOnPublished() {
        DataContract contract = new DataContract();
        contract.setId(UUID.randomUUID());
        contract.setTenantId(tenantId);
        contract.setName("Published Contract");
        contract.setStatus(ContractStatus.PUBLISHED);

        when(repository.findByIdAndTenantId(contract.getId(), tenantId)).thenReturn(Optional.of(contract));

        DataContractUpdateRequest updateRequest = new DataContractUpdateRequest("New Name", null, null, null, null);

        assertThrows(IllegalStateException.class, () -> service.update(contract.getId(), updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown contract")
    void shouldThrowNotFoundForUnknownContract() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(unknownId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(unknownId));
    }

    @Test
    @DisplayName("Should throw when no tenant context is available")
    void shouldThrowWhenNoTenantContext() {
        TenantContext.clear();

        DataContractCreateRequest request = new DataContractCreateRequest("Test", null, null, null, null);

        assertThrows(IllegalStateException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("Should update a draft contract")
    void shouldUpdateDraftContract() {
        DataContract contract = new DataContract();
        contract.setId(UUID.randomUUID());
        contract.setTenantId(tenantId);
        contract.setName("Original Name");
        contract.setStatus(ContractStatus.DRAFT);

        when(repository.findByIdAndTenantId(contract.getId(), tenantId)).thenReturn(Optional.of(contract));
        when(repository.save(any(DataContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DataContractUpdateRequest updateRequest = new DataContractUpdateRequest(
                "Updated Name", "New description", null, null, null);

        DataContractResponse response = service.update(contract.getId(), updateRequest);

        assertEquals("Updated Name", response.name());
        assertEquals("New description", response.description());
    }
}
