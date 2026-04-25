package com.projectnexus.contracts.interfaces;

import com.projectnexus.contracts.application.DataContractService;
import com.projectnexus.contracts.application.dto.DataContractCreateRequest;
import com.projectnexus.contracts.application.dto.DataContractResponse;
import com.projectnexus.contracts.application.dto.DataContractUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Data Contract operations.
 *
 * <p>Thin layer — delegates all business logic to {@link DataContractService}.
 * All endpoints are tenant-scoped via the JWT-based tenant context.
 *
 * <p><strong>Authorization (Phase 2):</strong> Endpoints will be secured with:
 * <ul>
 *   <li>{@code @PreAuthorize("hasRole('UPSTREAM_ADMIN')")} for create/update/publish</li>
 *   <li>{@code @PreAuthorize("hasAnyRole('UPSTREAM_ADMIN','DOWNSTREAM_CONSUMER','VIEWER')")} for read</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Data Contracts", description = "Upstream-owned data product governance — create, version, and publish contracts")
public class DataContractController {

    private final DataContractService dataContractService;

    // Phase 2: @PreAuthorize("hasRole('UPSTREAM_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new Data Contract", description = "Creates a DRAFT contract with optional test variables, business goals, and sharing rules")
    public ResponseEntity<DataContractResponse> create(@Valid @RequestBody DataContractCreateRequest request) {
        DataContractResponse response = dataContractService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Phase 2: @PreAuthorize("hasAnyRole('UPSTREAM_ADMIN','DOWNSTREAM_CONSUMER','VIEWER')")
    @GetMapping
    @Operation(summary = "List all contracts for current tenant", description = "Returns paginated list of all contracts (DRAFT and PUBLISHED) for the authenticated tenant")
    public ResponseEntity<Page<DataContractResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(dataContractService.listForCurrentTenant(pageable));
    }

    // Phase 2: @PreAuthorize("hasAnyRole('UPSTREAM_ADMIN','DOWNSTREAM_CONSUMER','VIEWER')")
    @GetMapping("/published")
    @Operation(summary = "List published contracts", description = "Returns only PUBLISHED contracts — used by downstream teams for alignment expectations")
    public ResponseEntity<Page<DataContractResponse>> listPublished(
            @PageableDefault(size = 20, sort = "publishedAt") Pageable pageable) {
        return ResponseEntity.ok(dataContractService.listPublished(pageable));
    }

    // Phase 2: @PreAuthorize("hasAnyRole('UPSTREAM_ADMIN','DOWNSTREAM_CONSUMER','VIEWER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get a contract by ID", description = "Returns a single contract with all test variables")
    public ResponseEntity<DataContractResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dataContractService.getById(id));
    }

    // Phase 2: @PreAuthorize("hasRole('UPSTREAM_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a draft contract", description = "Updates a DRAFT contract. Published contracts cannot be modified — create a new version instead.")
    public ResponseEntity<DataContractResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DataContractUpdateRequest request) {
        return ResponseEntity.ok(dataContractService.update(id, request));
    }

    // Phase 2: @PreAuthorize("hasRole('UPSTREAM_ADMIN')")
    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a contract", description = "Transitions a DRAFT contract to PUBLISHED, making it immutable and available for downstream alignment")
    public ResponseEntity<DataContractResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(dataContractService.publish(id));
    }
}
