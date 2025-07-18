package com.llmserver.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

// Rough sructure of the request's json:
/*
"contents": [
    {"role": "user", "parts": [{"text": "Your prompt here"}]}
]
*/

public class LlmDto {
    
    // Frontend
    public record PromptRequest(
        @NotBlank(message = "Prompt cannot be empty")
        String prompt,

        @NotBlank(message = "User ID cannot be empty")
        String userId,
        
        String conversationId
    ) {}
    public record PromptResponse(String text, String conversationId) {}

    // LLM Service
    public record Part(String text) {};
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Content(List<Part> parts, String role) {};
    public record LlmRequest(List<Content> contents) {};
    
    public record ResponsePart(String text) {}
    public record ResponseContent(List<ResponsePart> parts, String role) {}
    public record Candidate(ResponseContent content) {}
    public record LlmResponse(List<Candidate> candidates) {}
}