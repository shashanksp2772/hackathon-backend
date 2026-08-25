package com.hackathon.backend.routing;

/**
 * One candidate agent for an order, ranked by whichever strategy produced it.
 * The compact constructor enforces the confidence/reasoning contract once,
 * here, instead of every {@link RoutingStrategy} implementation re-validating
 * its own output.
 */
public record RoutingRecommendation(String agentId, double confidence, String reasoning) {

    public RoutingRecommendation {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId is required");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be within [0.0, 1.0], got: " + confidence);
        }
        if (reasoning == null || reasoning.isBlank()) {
            throw new IllegalArgumentException("reasoning is required");
        }
    }
}
