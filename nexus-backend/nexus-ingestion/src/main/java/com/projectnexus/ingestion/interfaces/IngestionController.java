package com.projectnexus.ingestion.interfaces;

import com.projectnexus.ingestion.application.PayloadService;
import com.projectnexus.ingestion.application.dto.DeviationResponse;
import com.projectnexus.ingestion.application.dto.PayloadResponse;
import com.projectnexus.ingestion.application.dto.PayloadSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for payload ingestion and deviation queries.
 */
@RestController
@RequestMapping("/api/v1/payloads")
@Tag(name = "Payload Ingestion", description = "Submit payloads and query validation results")
@RequiredArgsConstructor
public class IngestionController {

    private final PayloadService payloadService;

    @PostMapping
    @Operation(summary = "Submit a payload", description = "Submit a data payload against a published Data Contract for async validation")
    public ResponseEntity<PayloadResponse> submit(@Valid @RequestBody PayloadSubmitRequest request) {
        PayloadResponse response = payloadService.submit(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payload by ID", description = "Retrieve a specific payload's details and processing status")
    public PayloadResponse getById(@PathVariable UUID id) {
        return payloadService.getById(id);
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "List payloads by contract", description = "Paginated list of payloads submitted against a specific contract")
    public Page<PayloadResponse> listByContract(@PathVariable UUID contractId, @ParameterObject Pageable pageable) {
        return payloadService.listByContract(contractId, pageable);
    }

    @GetMapping("/{id}/deviations")
    @Operation(summary = "Get deviations for a payload", description = "Paginated list of deviations detected for a specific payload")
    public Page<DeviationResponse> getDeviationsForPayload(@PathVariable UUID id, @ParameterObject Pageable pageable) {
        return payloadService.getDeviationsForPayload(id, pageable);
    }

    @GetMapping("/deviations/by-contract/{contractId}")
    @Operation(summary = "List deviations by contract", description = "Paginated list of all deviations for payloads under a specific contract")
    public Page<DeviationResponse> listDeviationsByContract(@PathVariable UUID contractId, @ParameterObject Pageable pageable) {
        return payloadService.listDeviationsByContract(contractId, pageable);
    }
}
