package com.hackathon.backend.ai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * The shape Ollama's structured output is constrained to (see
 * {@link OllamaClient}'s JSON schema) and what gets deserialized into.
 * Re-validated with Spring's own {@code jakarta.validation.Validator} in
 * {@link AiAdvisorService} — schema-constrained decoding guarantees shape,
 * these annotations guarantee content (range, blankness).
 */
record AiRecommendationPayload(

        @NotBlank(message = "agentId is required")
        String agentId,

        @NotNull(message = "confidence is required")
        @DecimalMin(value = "0.0", message = "confidence must be >= 0.0")
        @DecimalMax(value = "1.0", message = "confidence must be <= 1.0")
        Double confidence,

        @NotBlank(message = "reasoning is required")
        String reasoning
) {
}
