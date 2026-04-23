package com.projectnexus.common.config;

import com.projectnexus.common.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * Enables Spring Data JPA auditing for automatic population of
 * {@code createdBy} and {@code updatedBy} fields on all entities
 * extending {@link com.projectnexus.common.entity.BaseTenantEntity}.
 *
 * <p>The current auditor (user ID) is resolved from {@link TenantContext},
 * which is populated by the JWT authentication pipeline on every request.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    /**
     * Provides the current user UUID for JPA auditing annotations
     * ({@code @CreatedBy}, {@code @LastModifiedBy}).
     *
     * @return an {@link AuditorAware} backed by the thread-local tenant context
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> Optional.ofNullable(TenantContext.getCurrentUser());
    }
}
