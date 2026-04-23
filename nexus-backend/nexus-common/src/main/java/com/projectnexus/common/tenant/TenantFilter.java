package com.projectnexus.common.tenant;

import com.projectnexus.common.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that extracts the tenant ID from the JWT on every request
 * and populates {@link TenantContext}.
 *
 * <p>Runs after the JWT authentication filter (Order 2) so the security context
 * is already populated. This filter (Order 3) reads the tenant claim and sets
 * the thread-local context used by downstream services and the
 * {@link TenantHolder} for PostgreSQL RLS.
 *
 * <p>The tenant context is always cleared in a {@code finally} block to prevent
 * cross-request leakage on pooled servlet threads.
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                UUID tenantId = jwtTokenProvider.extractTenantId(token);
                UUID userId = jwtTokenProvider.extractUserId(token);

                TenantContext.setCurrentTenant(tenantId);
                TenantContext.setCurrentUser(userId);

                log.debug("Tenant context set from JWT: tenantId={}, userId={}", tenantId, userId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Extracts the Bearer token from the Authorization header.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
