package com.projectnexus.alignment.interfaces;

import com.projectnexus.alignment.application.AlignmentService;
import com.projectnexus.alignment.application.dto.AlignmentExpectationCreateRequest;
import com.projectnexus.alignment.application.dto.AlignmentExpectationResponse;
import com.projectnexus.alignment.application.dto.AlignmentExpectationUpdateRequest;
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
 * REST controller for Alignment Expectation operations.
 *
 * <p>Downstream teams use these endpoints to define and manage expectations
 * against published Data Contracts.
 *
 * <p><strong>Authorization (Phase 2):</strong>
 * <ul>
 *   <li>Create/update/toggle: {@code @PreAuthorize("hasAnyRole('DOWNSTREAM_CONSUMER','UPSTREAM_ADMIN')")}</li>
 *   <li>Read: {@code @PreAuthorize("hasAnyRole('DOWNSTREAM_CONSUMER','UPSTREAM_ADMIN','VIEWER')")}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/expectations")
@RequiredArgsConstructor
@Tag(name = "Alignment Expectations", description = "Downstream-owned expectations against published Data Contracts")
public class AlignmentController {

    private final AlignmentService alignmentService;

    @PostMapping
    @Operation(summary = "Create an expectation", description = "Creates an expectation against a PUBLISHED Data Contract")
    public ResponseEntity<AlignmentExpectationResponse> create(
            @Valid @RequestBody AlignmentExpectationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alignmentService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all expectations for current tenant")
    public ResponseEntity<Page<AlignmentExpectationResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(alignmentService.listForCurrentTenant(pageable));
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "List expectations for a specific contract")
    public ResponseEntity<Page<AlignmentExpectationResponse>> listByContract(
            @PathVariable UUID contractId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(alignmentService.listByContract(contractId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expectation by ID")
    public ResponseEntity<AlignmentExpectationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(alignmentService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expectation", description = "Updates name, description, severity, or rule expression")
    public ResponseEntity<AlignmentExpectationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AlignmentExpectationUpdateRequest request) {
        return ResponseEntity.ok(alignmentService.update(id, request));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an expectation", description = "Stops evaluating this expectation on incoming payloads")
    public ResponseEntity<AlignmentExpectationResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(alignmentService.deactivate(id));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Reactivate an expectation")
    public ResponseEntity<AlignmentExpectationResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(alignmentService.activate(id));
    }
}
