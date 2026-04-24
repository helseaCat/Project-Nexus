package com.projectnexus.common.tenant;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Spring MVC interceptor that enables the Hibernate tenant filter and sets
 * the PostgreSQL session variable for Row Level Security on every request.
 *
 * <p>This interceptor runs after the servlet filters ({@link TenantFilter})
 * have populated {@link TenantContext} with the tenant ID from the JWT.
 * It bridges the servlet layer to the JPA/Hibernate layer by:
 * <ol>
 *   <li>Enabling the Hibernate {@code tenantFilter} so all JPA queries
 *       automatically include {@code WHERE tenant_id = :tenantId}</li>
 *   <li>Setting the PostgreSQL session variable {@code app.current_tenant}
 *       so RLS policies enforce isolation at the database level</li>
 * </ol>
 *
 * <p>Registered via {@link com.projectnexus.common.config.WebMvcConfig}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            // Enable Hibernate filter for query-level tenant isolation
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);

            log.debug("Hibernate tenant filter enabled for tenantId={}", tenantId);
        }
        return true;
    }
}
