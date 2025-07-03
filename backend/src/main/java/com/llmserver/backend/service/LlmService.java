package com.llmserver.backend.service;

import com.llmserver.backend.dto.LlmDto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Collections;
import java.util.List;

@Service
public class LlmService {

    private final RestClient restClient;
 
    // @param webClientBuilder The WebClient builder provided by Spring Boot.
    // @param apiUrl The URL of the LLM API, injected from application.properties.
    // @param apiKey The API key for the LLM API, injected from application.properties.
    public LlmService(
        RestClient.Builder restClientBuilder,
        @Value("${llm.api.url}") String apiUrl,
        @Value("${llm.api.key}") String apiKey
    ) {
        this.restClient = restClientBuilder
            .baseUrl(apiUrl)
            .defaultUriVariables(Collections.singletonMap("key", apiKey))
            .build()
            ;
    }

    // @param prompt The user's prompt text.
    // @return The LLM's response text.
    public String promptLlm(String prompt) {
        Part part = new Part(prompt);
        Content content = new Content(Collections.singletonList(part), "user");
        LlmRequest llmRequest = new LlmRequest(Collections.singletonList(content));

        try {
            LlmResponse response = restClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", "{key}").build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(llmRequest)
                    .retrieve()
                    .body(LlmResponse.class);
                    
            return extractTextFromResponse(response);
        } catch (Exception e) {
            System.err.println("Error calling LLM API: " + e.getMessage());
            return "Could not get a response from the LLM.";
        }
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
        return "Unable to get a response.";
    }
}