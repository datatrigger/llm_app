package com.llmserver.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.Customizer;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable) // Useless for stateless REST APIs
            .formLogin(AbstractHttpConfigurer::disable) // No login for now
            // TODO .httpBasic(AbstractHttpConfigurer::disable)
            
            // Stateless sessions (REST API deployed in ephemeral containers)
            .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Explicit and future-proof authorizations
            .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/llm/prompt").permitAll()
            .anyRequest()
            .denyAll()
            )
            
            // Security headers
            .headers(headers -> headers)
            .contentTypeOptions(Customizer.withDefaults())
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true)
            )
            .build();
    }
}