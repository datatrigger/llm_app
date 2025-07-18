package com.llmserver.backend.config;

import com.llmserver.backend.filter.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int HSTS_MAX_AGE_ONE_YEAR = 365 * 24 * 60 * 60;

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable) // Useless for stateless REST APIs
            .formLogin(AbstractHttpConfigurer::disable) // No login for now
            
            // Add API key filter before Spring Security filters
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Stateless sessions (REST API deployed in ephemeral containers)
            .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Explicit and future-proof authorizations
            .authorizeHttpRequests(
                authz -> authz
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/llm/prompt").permitAll() // Protected by the api key filter
                .anyRequest()
                .denyAll()
            )
            
            // Security headers
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(HSTS_MAX_AGE_ONE_YEAR)
                    .includeSubDomains(true)
                    .preload(true)
                )
            )
            .build();
    }
}