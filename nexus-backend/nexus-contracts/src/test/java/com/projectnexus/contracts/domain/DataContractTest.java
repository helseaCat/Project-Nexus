package com.projectnexus.contracts.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DataContract} domain logic.
 */
class DataContractTest {

    @Test
    @DisplayName("Should publish a draft contract")
    void shouldPublishDraft() {
        DataContract contract = new DataContract();
        contract.setStatus(ContractStatus.DRAFT);

        contract.publish();

        assertEquals(ContractStatus.PUBLISHED, contract.getStatus());
        assertNotNull(contract.getPublishedAt());
    }

    @Test
    @DisplayName("Should reject publishing an already published contract")
    void shouldRejectDoublePublish() {
        DataContract contract = new DataContract();
        contract.setStatus(ContractStatus.DRAFT);
        contract.publish();

        assertThrows(IllegalStateException.class, contract::publish);
    }

    @Test
    @DisplayName("Should manage test variables bidirectionally")
    void shouldManageTestVariables() {
        DataContract contract = new DataContract();
        TestVariable variable = new TestVariable();
        variable.setName("pressure");

        contract.addTestVariable(variable);

        assertEquals(1, contract.getTestVariables().size());
        assertEquals(contract, variable.getDataContract());

        contract.removeTestVariable(variable);

        assertEquals(0, contract.getTestVariables().size());
        assertNull(variable.getDataContract());
    }

    @Test
    @DisplayName("Should default to DRAFT status and version 1")
    void shouldDefaultToDraftAndVersion1() {
        DataContract contract = new DataContract();

        assertEquals(ContractStatus.DRAFT, contract.getStatus());
        assertEquals(1, contract.getVersion());
        assertNull(contract.getPublishedAt());
    }
}
