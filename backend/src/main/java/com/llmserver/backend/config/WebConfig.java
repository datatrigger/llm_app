package com.llmserver.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/llm/prompt")
                .allowedOrigins(
                    "https://upgraded-goggles-467wrr974762qwv7-4200.app.github.dev",
                    "https://taliandorogd.netlify.app",
                    "https://assistant.vlg.engineer"
                )
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true);
    }
}