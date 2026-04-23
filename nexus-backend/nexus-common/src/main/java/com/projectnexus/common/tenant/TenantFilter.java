package com.projectnexus.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts tenant ID from the authenticated principal and sets it
 * in TenantContext + PostgreSQL session variable for RLS.
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // TODO: Extract tenant ID from SecurityContext (JWT claims)
            // TenantContext.setCurrentTenant(tenantId);
            // Set PostgreSQL session variable: SET app.current_tenant = 'tenantId'
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
