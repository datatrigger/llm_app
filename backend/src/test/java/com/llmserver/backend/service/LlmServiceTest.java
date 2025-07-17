package com.llmserver.backend.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;

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
        wireMockServer.stubFor(post(urlEqualTo(API_PATH))
            .willReturn(aResponse()
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
                    """.formatted(expectedResponse))));

        // When
        String result = llmService.promptLlmWithHistory("Hello", List.of());

        // Then
        assertEquals(expectedResponse, result);
        
        // Verify the request was made correctly
        wireMockServer.verify(postRequestedFor(urlEqualTo(API_PATH))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(matchingJsonPath("$.contents[0].parts[0].text", equalTo("Hello")))
            .withRequestBody(matchingJsonPath("$.contents[0].role", equalTo("user"))));
    }
}