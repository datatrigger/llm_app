package com.llmserver.backend.service;

import com.llmserver.backend.dto.LlmDto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;

@Service
public class LlmService {

    private final WebClient webClient;
    private final String apiKey;
 
    // @param webClientBuilder The WebClient builder provided by Spring Boot.
    // @param apiUrl The URL of the LLM API, injected from application.properties.
    // @param apiKey The API key for the LLM API, injected from application.properties.
    public LlmService(
        WebClient.Builder webClientBuilder,
        @Value("${llm.api.url}") String apiUrl,
        @Value("${llm.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
    }

    // @param prompt The user's prompt text.
    // @return A Mono emitting the LLM's response text.
    public Mono<String> promptLlmm(String prompt) {
        Part part = new Part(prompt);
        Content content = new Content(Collections.singletonList(part), "user");
        LlmRequest llmRequest = new LlmRequest(Collections.singletonList(content));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(llmRequest)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .map(this::extractTextFromResponse);
    }

    // @param response The LlmResponse object from the API.
    // @return The generated text, or an empty string if not found.
    private String extractTextFromResponse(LlmResponse response) {
        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            Candidate firstCandidate = response.candidates().get(0);
            if (firstCandidate.content() != null && firstCandidate.content().parts() != null && !firstCandidate.content().parts().isEmpty()) {
                return firstCandidate.content().parts().get(0).text();
            }
        }
        return "Sorry, I couldn't get a response.";
    }
}