package com.hackathon.backend.ai;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.order.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Two genuinely different documents, not one template with a field
 * appended. The re-plan prompt is a situation report — it names the
 * outage, the resulting pressure, and asks for recovery-framed reasoning —
 * because a first assignment and a recovery are different situations and
 * the model should know which one it's in.
 */
@Component
class PromptBuilder {

    private static final String RESPONSE_CONTRACT = """
            Respond with ONLY a JSON object shaped exactly like this, no extra text:
            {"agentId": "<one of the agent ids above>", "confidence": <number between 0.0 and 1.0>, "reasoning": "<one or two sentences an ops person can act on>"}""";

    private static final String INITIAL_TEMPLATE = """
            You are a delivery-routing assistant for ZipRun, a delivery service.

            SITUATION: First assignment. A new order needs to be assigned to one agent from the roster below.

            ORDER:
            - id: %s
            - description: %s

            AVAILABLE AGENTS:
            %s

            TASK: Choose exactly one agent id from the list above best suited for this order. All else equal, prefer the agent with fewer active orders.

            %s
            """;

    private static final String REPLAN_TEMPLATE = """
            You are a delivery-routing assistant for ZipRun, a delivery service.

            SITUATION: RECOVERY. Agent %s has just gone OFFLINE. Their assignment to this order is void, and %d order(s) total are now stranded by this outage - you are reassigning one of them.

            ORDER:
            - id: %s
            - description: %s

            AVAILABLE AGENTS (the offline agent is excluded from this list):
            %s

            TASK: Choose exactly one agent id from the list above to absorb this stranded order. Your reasoning should reflect that this is a recovery from an outage, not a routine first assignment.

            %s
            """;

    String buildInitialPrompt(Order order, List<Agent> availableAgents) {
        return INITIAL_TEMPLATE.formatted(
                order.getId(), order.getDescription(), rosterOf(availableAgents), RESPONSE_CONTRACT);
    }

    String buildReplanPrompt(Order order, List<Agent> availableAgents, String offlineAgentId, int strandedOrderCount) {
        return REPLAN_TEMPLATE.formatted(
                offlineAgentId, strandedOrderCount, order.getId(), order.getDescription(),
                rosterOf(availableAgents), RESPONSE_CONTRACT);
    }

    private String rosterOf(List<Agent> agents) {
        return agents.stream()
                .map(agent -> "- id: %s, name: %s, activeOrders: %d".formatted(
                        agent.getId(), agent.getName(), agent.getActiveOrderCount()))
                .collect(Collectors.joining("\n"));
    }
}
