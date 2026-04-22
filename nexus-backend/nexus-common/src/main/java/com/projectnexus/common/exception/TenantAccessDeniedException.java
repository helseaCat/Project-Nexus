package com.projectnexus.common.exception;

public class TenantAccessDeniedException extends RuntimeException {

    public TenantAccessDeniedException() {
        super("Access denied: tenant isolation violation");
    }

    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
