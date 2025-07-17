package com.llmserver.backend.service;

import com.llmserver.backend.dto.LlmDto.*;
import com.llmserver.backend.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;

@Service
public class LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    private final RestClient restClient;
    private final String llmBaseUrl;
 
    public LlmService(
        RestClient.Builder restClientBuilder,
        @Value("${llm.base.url}") String baseUrl,
        @Value("${llm.api.path}") String apiPath
    ) {
        this.llmBaseUrl = baseUrl;
        this.restClient = restClientBuilder
            .baseUrl(baseUrl + apiPath)
            .build();
        logger.info("LlmService initialized", "baseUrl", baseUrl, "apiPath", apiPath);
    }

    private String getIdTokenForCloudRun(String targetUrl) {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            IdTokenProvider idTokenProvider = (IdTokenProvider) credentials;
            IdTokenCredentials idTokenCredentials = IdTokenCredentials
                .newBuilder()
                .setIdTokenProvider(idTokenProvider)
                .setTargetAudience(targetUrl)
                .build();
            idTokenCredentials.refreshIfExpired();
            logger.debug("Successfully obtained ID token for Cloud Run");
            return idTokenCredentials.getIdToken().getTokenValue();
        } catch (IOException e) {
            logger.error("Failed to get ID token for Cloud Run", e);
            return "";
        }
    }

    public String promptLlmWithHistory(String prompt, List<Message> conversationHistory) {
        logger.info("Calling LLM", 
            "promptLength", prompt.length(),
            "historySize", conversationHistory.size());

        List<Content> contents = new ArrayList<>();
        
        // Add conversation history
        for (Message message : conversationHistory) {
            Part part = new Part(message.getText());
            Content content = new Content(Collections.singletonList(part), message.getRole());
            contents.add(content);
        }
        
        // Add current user prompt
        Part currentPart = new Part(prompt);
        Content currentContent = new Content(Collections.singletonList(currentPart), "user");
        contents.add(currentContent);
        
        LlmRequest llmRequest = new LlmRequest(contents);
        logger.debug("Prepared LLM request", "totalContents", contents.size());

        try {
            // Cloud Run service-to-service authentication
            String idToken = getIdTokenForCloudRun(llmBaseUrl);
            if (idToken.isEmpty()) {
                logger.error("Failed to obtain ID token for Cloud Run authentication");
                throw new RuntimeException("Failed to obtain ID token for Cloud Run authentication");
            }
            
            long startTime = System.currentTimeMillis();
            LlmResponse response = restClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + idToken)
                    .body(llmRequest)
                    .retrieve()
                    .body(LlmResponse.class);
            
            long duration = System.currentTimeMillis() - startTime;
            String responseText = extractTextFromResponse(response);
            
            logger.info("LLM call completed successfully", 
                "duration", duration,
                "responseLength", responseText.length());
            
            return responseText;
            
        } catch (Exception e) {
            logger.error("Error calling LLM API", e);
            return "Could not get a response from the LLM.";
        }
    }

    private String extractTextFromResponse(LlmResponse response) {
        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            Candidate firstCandidate = response.candidates().get(0);
            if (firstCandidate.content() != null && firstCandidate.content().parts() != null && !firstCandidate.content().parts().isEmpty()) {
                return firstCandidate.content().parts().get(0).text();
            }
        }
        logger.warn("Unable to extract text from LLM response");
        return "Unable to get a response.";
    }
}