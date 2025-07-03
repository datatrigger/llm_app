package com.llmserver.backend.controller;

import com.llmserver.backend.dto.LlmDto.PromptRequest;
import com.llmserver.backend.dto.LlmDto.PromptResponse;
import com.llmserver.backend.entity.Message;
import com.llmserver.backend.service.LlmService;
import com.llmserver.backend.service.ConversationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;
    private final ConversationService conversationService;

    public LlmController(LlmService llmService, ConversationService conversationService) {
        this.llmService = llmService;
        this.conversationService = conversationService;
    }

    // @param request The request from the frontend containing:
    // the user's prompt, user ID, and optional conversation ID.
    // @return The response for the frontend with the LLM response and conversation ID.
    @PostMapping("/prompt")
    public ResponseEntity<PromptResponse> getLlmResponse(@RequestBody PromptRequest request) {
        try {
            // Validate required fields
            if (request.prompt() == null || request.prompt().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.userId() == null || request.userId().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String conversationId = request.conversationId();
            List<Message> conversationHistory;
            
            // Check if this is a new conversation or continuing an existing one
            if (conversationId == null || conversationId.trim().isEmpty()) {
                // New conversation - start with empty history
                conversationHistory = List.of();
            } else {
                // Existing conversation - retrieve history
                if (!conversationService.conversationExists(request.userId(), conversationId)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                conversationHistory = conversationService.getConversationHistory(request.userId(), conversationId);
            }
            
            // Get LLM response with conversation history
            String llmResponseText = llmService.promptLlmWithHistory(request.prompt(), conversationHistory);
            
            // Create message objects
            Message userMessage = new Message(request.prompt(), "user");
            Message modelMessage = new Message(llmResponseText, "model");
            
            // Save messages to Firestore
            if (conversationId == null || conversationId.trim().isEmpty()) {
                // New conversation - create it with the first user message
                conversationId = conversationService.createConversation(request.userId(), userMessage);
                // Add the model's response
                conversationService.addMessageToConversation(request.userId(), conversationId, modelMessage);
            } else {
                // Existing conversation - add both messages
                conversationService.addMessageToConversation(request.userId(), conversationId, userMessage);
                conversationService.addMessageToConversation(request.userId(), conversationId, modelMessage);
            }
            
            return ResponseEntity.ok(new PromptResponse(llmResponseText, conversationId));
            
        } catch (Exception e) {
            System.err.println("Error processing prompt request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}