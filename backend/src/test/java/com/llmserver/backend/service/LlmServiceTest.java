package com.llmserver.backend.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.llmserver.backend.entity.Message;

@SpringBootTest()
@EnableWireMock({
    @ConfigureWireMock(
        name = "llm-server",
        baseUrlProperties = "llm.base.url"
    )
})
class LlmServiceTest {
    
    @Autowired
    private LlmService llmService;

    @InjectWireMock("llm-server")
    private WireMockServer wireMockServer;

    private static final String API_PATH = "/v1beta/models/gemma-3-4b-it:generateContent";

    @Test
    void shouldReturnLlmResponseSuccessfully() {
        // Given
        String expectedResponse = "Hello! How can I help you today?";
        
        // Mock the LLM API response
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "candidates": [
                                {
                                    "content": {
                                        "parts": [
                                            {
                                                "text": "%s"
                                            }
                                        ],
                                        "role": "model"
                                    }
                                }
                            ]
                        }
                        """.formatted(expectedResponse)
                    )
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Hello", List.of());

        // Then
        assertEquals(expectedResponse, result);
        
        // Verify the request was made correctly
        wireMockServer
            .verify(postRequestedFor(urlEqualTo(API_PATH))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(matchingJsonPath("$.contents[0].parts[0].text", equalTo("Hello")))
            .withRequestBody(matchingJsonPath("$.contents[0].role", equalTo("user"))));
    }

    @Test
    void shouldIncludeConversationHistoryInRequest() {
        // Given
        List<Message> history = List.of(
            new Message("Previous user message", "user"),
            new Message("Previous model response", "model")
        );
        
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "candidates": [
                                {
                                    "content": {
                                        "parts": [{"text": "Response with context"}],
                                        "role": "model"
                                    }
                                }
                            ]
                        }
                        """
                    )
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Current message", history);

        // Then
        assertEquals("Response with context", result);
        
        // Verify history was included in request
        wireMockServer
            .verify(postRequestedFor(urlEqualTo(API_PATH))
            .withRequestBody(matchingJsonPath("$.contents[0].parts[0].text", equalTo("Previous user message")))
            .withRequestBody(matchingJsonPath("$.contents[0].role", equalTo("user")))
            .withRequestBody(matchingJsonPath("$.contents[1].parts[0].text", equalTo("Previous model response")))
            .withRequestBody(matchingJsonPath("$.contents[1].role", equalTo("model")))
            .withRequestBody(matchingJsonPath("$.contents[2].parts[0].text", equalTo("Current message")))
            .withRequestBody(matchingJsonPath("$.contents[2].role", equalTo("user"))));
    }

    @Test
    void shouldReturnErrorMessageWhenApiCallFails() {
        // Given
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Internal server error\"}")
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Test message", List.of());

        // Then
        assertEquals("Could not get a response from the LLM.", result);
    }

    @Test
    void shouldHandleEmptyResponseGracefully() {
        // Given
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"candidates\": []}")
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Test message", List.of());

        // Then
        assertEquals("Unable to get a response.", result);
    }

    @Test
    void shouldHandleMalformedResponseGracefully() {
        // Given
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"candidates\": [{}]}")
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Test message", List.of());

        // Then
        assertEquals("Unable to get a response.", result);
    }

    @Test
    void shouldHandleNetworkTimeoutGracefully() {
        // Given
        wireMockServer
            .stubFor(
                post(urlEqualTo(API_PATH))
                .willReturn(
                    aResponse()
                    .withStatus(200)
                    .withFixedDelay(30000) // 30 second delay to simulate timeout
                    .withBody("{}")
                )
            )
        ;

        // When
        String result = llmService.promptLlmWithHistory("Test message", List.of());

        // Then
        assertTrue(
            result.contains("Could not get a response from the LLM.")
            || result.contains("Unable to get a response.")
        );
    }
}