package com.hackathon.backend.routing;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.order.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Deterministic fallback: recommend whoever has the fewest active orders.
 * No external dependencies, so it's always available even when the AI
 * strategy (T-3) isn't.
 */
@Component("rule-based")
public class RuleBasedRoutingStrategy implements RoutingStrategy {

    private static final String REASONING_TEMPLATE =
            "Recommended %s: fewest active orders among available agents (%d currently assigned).";

    @Override
    public List<RoutingRecommendation> recommend(Order order, List<Agent> availableAgents, RoutingContext context) {
        return availableAgents.stream()
                .sorted(Comparator.comparingInt(Agent::getActiveOrderCount))
                .map(agent -> new RoutingRecommendation(
                        agent.getId(),
                        1.0,
                        REASONING_TEMPLATE.formatted(agent.getName(), agent.getActiveOrderCount())))
                .toList();
    }
}
