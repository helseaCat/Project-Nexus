package com.projectnexus.common.security;

import org.springframework.stereotype.Component;

/**
 * JWT token generation and validation.
 * Tokens contain tenant_id, user_id, and roles.
 */
@Component
public class JwtTokenProvider {

    // TODO: Implement JWT generation, validation, and claim extraction
    // - generateToken(userId, tenantId, roles)
    // - validateToken(token)
    // - extractTenantId(token)
    // - extractUserId(token)
}
