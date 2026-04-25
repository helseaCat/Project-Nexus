package com.projectnexus.contracts.interfaces;

import com.projectnexus.contracts.application.DataContractService;
import com.projectnexus.contracts.application.dto.DataContractCreateRequest;
import com.projectnexus.contracts.application.dto.DataContractResponse;
import com.projectnexus.contracts.application.dto.DataContractUpdateRequest;
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
 */
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class DataContractController {

    private final DataContractService dataContractService;

    @PostMapping
    public ResponseEntity<DataContractResponse> create(@Valid @RequestBody DataContractCreateRequest request) {
        DataContractResponse response = dataContractService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DataContractResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(dataContractService.listForCurrentTenant(pageable));
    }

    @GetMapping("/published")
    public ResponseEntity<Page<DataContractResponse>> listPublished(
            @PageableDefault(size = 20, sort = "publishedAt") Pageable pageable) {
        return ResponseEntity.ok(dataContractService.listPublished(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataContractResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dataContractService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataContractResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DataContractUpdateRequest request) {
        return ResponseEntity.ok(dataContractService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<DataContractResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(dataContractService.publish(id));
    }
}
