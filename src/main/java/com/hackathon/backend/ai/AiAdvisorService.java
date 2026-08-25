package com.hackathon.backend.ai;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.order.Order;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.routing.RoutingRecommendation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the right prompt for the situation, calls the LLM, and turns its
 * response into a validated {@link RoutingRecommendation} — or throws
 * {@link AiGatewayException} at the first thing that goes wrong, leaving
 * the fallback decision to {@link AiRoutingStrategy}.
 */
@Service
@RequiredArgsConstructor
class AiAdvisorService {

    private final OllamaClient ollamaClient;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    RoutingRecommendation recommend(Order order, List<Agent> availableAgents, RoutingContext context) {
        String prompt = switch (context.triggerReason()) {
            case INITIAL -> promptBuilder.buildInitialPrompt(order, availableAgents);
            case AGENT_OFFLINE -> promptBuilder.buildReplanPrompt(
                    order, availableAgents, context.offlineAgentId(), context.strandedOrderCount());
        };

        String rawResponse = ollamaClient.requestStructuredCompletion(prompt);
        AiRecommendationPayload payload = parse(rawResponse);
        validate(payload);
        requireKnownAgent(payload, availableAgents);

        return new RoutingRecommendation(payload.agentId(), payload.confidence(), payload.reasoning());
    }

    private AiRecommendationPayload parse(String rawResponse) {
        try {
            return objectMapper.readValue(rawResponse, AiRecommendationPayload.class);
        } catch (JacksonException ex) {
            throw new AiGatewayException(AiFailureReason.MALFORMED_RESPONSE,
                    "Could not parse LLM response as JSON: " + rawResponse, ex);
        }
    }

    private void validate(AiRecommendationPayload payload) {
        Set<ConstraintViolation<AiRecommendationPayload>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new AiGatewayException(AiFailureReason.VALIDATION_FAILED, "LLM output failed validation: " + message);
        }
    }

    private void requireKnownAgent(AiRecommendationPayload payload, List<Agent> availableAgents) {
        boolean isKnownAgent = availableAgents.stream()
                .anyMatch(agent -> agent.getId().equals(payload.agentId()));
        if (!isKnownAgent) {
            throw new AiGatewayException(AiFailureReason.HALLUCINATED_AGENT,
                    "LLM recommended an agent not in the available roster: " + payload.agentId());
        }
    }
}
