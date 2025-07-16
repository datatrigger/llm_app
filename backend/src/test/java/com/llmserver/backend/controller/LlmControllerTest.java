package com.llmserver.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmserver.backend.dto.LlmDto.PromptRequest;
import com.llmserver.backend.entity.Message;
import com.llmserver.backend.service.ConversationService;
import com.llmserver.backend.service.LlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LlmController.class)
class LlmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlmService llmService;

    @MockBean
    private ConversationService conversationService;

    @Test
    void shouldCreateNewConversationSuccessfully() throws Exception {
        // Given
        PromptRequest request = new PromptRequest("Hello", "user123", null);
        when(llmService.promptLlmWithHistory(anyString(), anyList()))
                .thenReturn("Hello! How can I help you?");
        when(conversationService.createConversation(anyString(), any(Message.class)))
                .thenReturn("conv123");

        // When & Then
        mockMvc.perform(post("/api/llm/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Hello! How can I help you?"))
                .andExpect(jsonPath("$.conversationId").value("conv123"));
    }

    @Test
    void shouldContinueExistingConversationSuccessfully() throws Exception {
        // Given
        PromptRequest request = new PromptRequest("What's the weather?", "user123", "conv123");
        when(conversationService.conversationExists("user123", "conv123")).thenReturn(true);
        when(conversationService.getConversationHistory("user123", "conv123"))
                .thenReturn(Collections.singletonList(new Message("Hello", "user")));
        when(llmService.promptLlmWithHistory(anyString(), anyList()))
                .thenReturn("I don't have access to weather data.");

        // When & Then
        mockMvc.perform(post("/api/llm/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("I don't have access to weather data."))
                .andExpect(jsonPath("$.conversationId").value("conv123"));
    }

    @Test
    void shouldReturnBadRequestWhenPromptIsEmpty() throws Exception {
        // Given
        PromptRequest request = new PromptRequest("", "user123", null);

        // When & Then
        mockMvc.perform(post("/api/llm/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsEmpty() throws Exception {
        // Given
        PromptRequest request = new PromptRequest("Hello", "", null);

        // When & Then
        mockMvc.perform(post("/api/llm/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenConversationDoesNotExist() throws Exception {
        // Given
        PromptRequest request = new PromptRequest("Hello", "user123", "nonexistent");
        when(conversationService.conversationExists("user123", "nonexistent")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/llm/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}