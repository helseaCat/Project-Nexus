package com.projectnexus.alignment.infrastructure;

import com.projectnexus.alignment.domain.AlignmentExpectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlignmentExpectationRepository extends JpaRepository<AlignmentExpectation, UUID> {

    List<AlignmentExpectation> findByDataContractIdAndActiveTrue(UUID dataContractId);
}
