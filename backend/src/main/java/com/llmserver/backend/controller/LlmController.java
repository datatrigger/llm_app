package com.llmserver.backend.controller;

import com.llmserver.backend.dto.LlmDto.PromptRequest;
import com.llmserver.backend.dto.LlmDto.PromptResponse;
import com.llmserver.backend.service.LlmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    // @param request The request from the frontend containing the user's prompt.
    // @return The response for the frontend.
    @PostMapping("/prompt")
    public PromptResponse getLlmResponse(@RequestBody PromptRequest request) {
        String responseText = llmService.promptLlm(request.prompt());
        return new PromptResponse(responseText);
    }
}
