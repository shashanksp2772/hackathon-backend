package com.hackathon.backend.routing;

import com.hackathon.backend.suggestion.TriggerReason;

/**
 * Tells a {@link RoutingStrategy} what situation it's reasoning about — a
 * first assignment, or a recovery from an agent going offline — and, for
 * the latter, how much pressure the outage put on the system. The two
 * situations are different enough (see the AI strategy's re-plan prompt)
 * that this is modeled as an explicit field rather than inferred from the
 * order's current status.
 */
public record RoutingContext(TriggerReason triggerReason, String offlineAgentId, int strandedOrderCount) {

    public RoutingContext {
        if (triggerReason == TriggerReason.AGENT_OFFLINE && (offlineAgentId == null || offlineAgentId.isBlank())) {
            throw new IllegalArgumentException("offlineAgentId is required when triggerReason is AGENT_OFFLINE");
        }
    }

    public static RoutingContext initial() {
        return new RoutingContext(TriggerReason.INITIAL, null, 0);
    }

    public static RoutingContext agentOffline(String offlineAgentId, int strandedOrderCount) {
        return new RoutingContext(TriggerReason.AGENT_OFFLINE, offlineAgentId, strandedOrderCount);
    }

    public static RoutingContext agentOffline(String offlineAgentId) {
        return agentOffline(offlineAgentId, 1);
    }
}
