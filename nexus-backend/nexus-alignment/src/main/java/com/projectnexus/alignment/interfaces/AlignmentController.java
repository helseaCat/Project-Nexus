package com.projectnexus.alignment.interfaces;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Alignment Expectations and Payload ingestion.
 */
@RestController
@RequestMapping("/api/v1")
public class AlignmentController {

    // TODO: Inject AlignmentService
    // - POST   /expectations          → createExpectation
    // - GET    /expectations           → listExpectations
    // - POST   /payloads              → ingestPayload
    // - GET    /payloads/{id}         → getPayload
    // - GET    /deviations            → listDeviations
}
