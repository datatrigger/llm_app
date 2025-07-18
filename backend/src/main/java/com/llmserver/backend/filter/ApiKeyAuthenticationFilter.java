package com.llmserver.backend.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    
    private final String apiKey;

    public ApiKeyAuthenticationFilter(@Value("${api.key}") String apiKey) {
        this.apiKey = apiKey;
        logger.info("ApiKeyAuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        
        // Only protect the LLM prompt endpoint
        if (requestPath.equals("/api/llm/prompt")) {
            String providedApiKey = httpRequest.getHeader(API_KEY_HEADER);
            if (providedApiKey == null || !providedApiKey.equals(apiKey)) {
                logger.warn("Unauthorized request to protected endpoint", "path", requestPath, "hasApiKey", providedApiKey != null);
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Unauthorized - Invalid or missing API key\"}");
                return;
            }
            logger.debug("Authorized request to protected endpoint", "path", requestPath);
        }
        chain.doFilter(request, response);
    }
}