package com.projectnexus.contracts.interfaces;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Data Contract operations.
 * Thin layer — delegates to application services.
 */
@RestController
@RequestMapping("/api/v1/contracts")
public class DataContractController {

    // TODO: Inject DataContractService
    // - POST   /              → createContract
    // - GET    /              → listContracts
    // - GET    /{id}          → getContract
    // - PUT    /{id}          → updateContract
    // - POST   /{id}/publish  → publishContract
}
