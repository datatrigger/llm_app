package com.llmserver.backend.controller;

import com.llmserver.backend.dto.LlmDto.PromptRequest;
import com.llmserver.backend.dto.LlmDto.PromptResponse;
import com.llmserver.backend.service.LlmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST Controller to handle LLM-related requests from the frontend.
 */
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    // @param request The request from the frontend containing the user's prompt.
    // @return A Mono containing the response for the frontend.
    @PostMapping("/prompt")
    public Mono<PromptResponse> getLlmResponse(@RequestBody PromptRequest request) {
        return llmService.promptLlmm(request.prompt()).map(PromptResponse::new);
    }
}
