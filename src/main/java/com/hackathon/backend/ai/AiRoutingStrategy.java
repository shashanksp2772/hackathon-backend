package com.hackathon.backend.ai;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.order.Order;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.routing.RoutingRecommendation;
import com.hackathon.backend.routing.RoutingStrategy;
import com.hackathon.backend.routing.RuleBasedRoutingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The AI implementation of {@link RoutingStrategy}. Delegates to
 * {@link AiAdvisorService}; on any {@link AiGatewayException} (timeout,
 * malformed JSON, hallucinated agent id, failed validation) falls back to
 * the rule-based strategy and logs why, so a caller — on-demand endpoint
 * or the async re-plan listener alike — always gets back a usable
 * recommendation, never a silent drop.
 */
@Component("ai")
@RequiredArgsConstructor
@Slf4j
public class AiRoutingStrategy implements RoutingStrategy {

    private final AiAdvisorService aiAdvisorService;
    private final RuleBasedRoutingStrategy fallbackStrategy;

    @Override
    public List<RoutingRecommendation> recommend(Order order, List<Agent> availableAgents, RoutingContext context) {
        try {
            return List.of(aiAdvisorService.recommend(order, availableAgents, context));
        } catch (AiGatewayException ex) {
            log.warn("AI routing failed for order {} ({}): {} - falling back to rule-based",
                    order.getId(), ex.reason(), ex.getMessage());
            return fallbackStrategy.recommend(order, availableAgents, context);
        }
    }
}
