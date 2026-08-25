package com.hackathon.backend.ai;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Talks to a local Ollama server using its native {@code /api/chat}
 * endpoint rather than the OpenAI-compatible shim, specifically to use
 * {@code format} for JSON-schema-constrained decoding — the model is not
 * merely asked to return JSON, it is structurally unable to return
 * anything else. {@code think: false} suppresses reasoning-model preamble
 * (qwen3 is a thinking model) so {@code message.content} is pure JSON.
 *
 * Never touches the database or any repository — this class only ever
 * sees a text prompt in and returns text out.
 */
@Component
class OllamaClient {

    private static final JsonNode RECOMMENDATION_SCHEMA = parseSchema("""
            {
              "type": "object",
              "properties": {
                "agentId": {"type": "string"},
                "confidence": {"type": "number"},
                "reasoning": {"type": "string"}
              },
              "required": ["agentId", "confidence", "reasoning"]
            }
            """);

    private final RestClient restClient;
    private final OllamaProperties properties;

    OllamaClient(OllamaProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(properties.timeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    String requestStructuredCompletion(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.model(),
                "stream", false,
                "think", false,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "format", RECOMMENDATION_SCHEMA
        );

        Map<?, ?> response;
        try {
            response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
        } catch (ResourceAccessException ex) {
            throw new AiGatewayException(AiFailureReason.TIMEOUT,
                    "Ollama request timed out or the server is unreachable at " + properties.baseUrl(), ex);
        } catch (RestClientException ex) {
            throw new AiGatewayException(AiFailureReason.PROVIDER_ERROR, "Ollama request failed", ex);
        }

        try {
            var message = (Map<?, ?>) response.get("message");
            return (String) message.get("content");
        } catch (RuntimeException ex) {
            throw new AiGatewayException(AiFailureReason.MALFORMED_RESPONSE,
                    "Unexpected Ollama response shape: " + response, ex);
        }
    }

    private static JsonNode parseSchema(String schemaJson) {
        try {
            return new ObjectMapper().readTree(schemaJson);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
