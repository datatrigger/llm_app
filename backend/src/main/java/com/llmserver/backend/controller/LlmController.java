package com.llmserver.backend.controller;

import com.llmserver.backend.dto.LlmDto.PromptRequest;
import com.llmserver.backend.dto.LlmDto.PromptResponse;
import com.llmserver.backend.entity.Message;
import com.llmserver.backend.service.LlmService;
import com.llmserver.backend.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private static final Logger logger = LoggerFactory.getLogger(LlmController.class);
    private final LlmService llmService;
    private final ConversationService conversationService;

    public LlmController(LlmService llmService, ConversationService conversationService) {
        this.llmService = llmService;
        this.conversationService = conversationService;
    }

    @PostMapping("/prompt")
    public ResponseEntity<PromptResponse> getLlmResponse(@RequestBody PromptRequest request) {
        // Generate request ID for tracing
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("userId", request.userId());
        
        logger.info("Received prompt request", 
            "conversationId", request.conversationId(),
            "promptLength", request.prompt() != null ? request.prompt().length() : 0);

        try {
            // Validate required fields
            if (request.prompt() == null || request.prompt().trim().isEmpty()) {
                logger.warn("Invalid request: empty prompt");
                return ResponseEntity.badRequest().build();
            }
            if (request.userId() == null || request.userId().trim().isEmpty()) {
                logger.warn("Invalid request: empty userId");
                return ResponseEntity.badRequest().build();
            }

            String conversationId = request.conversationId();
            List<Message> conversationHistory;
            
            // Check if this is a new conversation or continuing an existing one
            if (conversationId == null || conversationId.trim().isEmpty()) {
                logger.info("Starting new conversation");
                conversationHistory = List.of();
            } else {
                logger.info("Continuing existing conversation", "conversationId", conversationId);
                if (!conversationService.conversationExists(request.userId(), conversationId)) {
                    logger.warn("Conversation not found", "conversationId", conversationId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                conversationHistory = conversationService.getConversationHistory(request.userId(), conversationId);
                logger.debug("Retrieved conversation history", "messageCount", conversationHistory.size());
            }
            
            // Get LLM response with conversation history
            String llmResponseText = llmService.promptLlmWithHistory(request.prompt(), conversationHistory);
            
            // Create message objects
            Message userMessage = new Message(request.prompt(), "user");
            Message modelMessage = new Message(llmResponseText, "model");
            
            // Save messages to Firestore
            if (conversationId == null || conversationId.trim().isEmpty()) {
                conversationId = conversationService.createConversation(request.userId(), userMessage);
                conversationService.addMessageToConversation(request.userId(), conversationId, modelMessage);
                logger.info("Created new conversation", "conversationId", conversationId);
            } else {
                conversationService.addMessageToConversation(request.userId(), conversationId, userMessage);
                conversationService.addMessageToConversation(request.userId(), conversationId, modelMessage);
                logger.info("Added messages to existing conversation");
            }
            
            logger.info("Successfully processed prompt request", 
                "responseLength", llmResponseText.length(),
                "conversationId", conversationId);
            
            return ResponseEntity.ok(new PromptResponse(llmResponseText, conversationId));
            
        } catch (Exception e) {
            logger.error("Error processing prompt request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            MDC.clear();
        }
    }
}