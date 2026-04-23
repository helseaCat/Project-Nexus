package com.projectnexus.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for Project-Nexus.
 *
 * <p>Configures stateless JWT-based authentication with:
 * <ul>
 *   <li>CSRF disabled (stateless API)</li>
 *   <li>Session management set to STATELESS</li>
 *   <li>JWT filter inserted before {@link UsernamePasswordAuthenticationFilter}</li>
 *   <li>Public endpoints for auth, health, and API docs</li>
 *   <li>All other endpoints require authentication</li>
 * </ul>
 *
 * <p>Currently configured as permit-all for MVP bootstrapping. Tighten the
 * authorization rules as authentication flows are implemented.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // MVP BOOTSTRAP: permit-all while auth flows are being built.
                        // Phase 2: Change to .authenticated() and add role-based rules:
                        //   .requestMatchers("/api/v1/contracts/**").hasAnyRole("ADMIN", "UPSTREAM")
                        //   .requestMatchers("/api/v1/expectations/**").hasAnyRole("ADMIN", "DOWNSTREAM")
                        //   .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
