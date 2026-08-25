package com.hackathon.backend.agentic;

import com.hackathon.backend.agent.AgentWentOfflineEvent;
import com.hackathon.backend.order.Order;
import com.hackathon.backend.order.OrderRepository;
import com.hackathon.backend.order.OrderStatus;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.suggestion.SuggestionGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * The agentic loop: observe (an agent goes OFFLINE) -> reason (which
 * specific orders are stranded) -> act (queue one suggestion per order,
 * reusing the same {@link SuggestionGenerationService} the on-demand
 * endpoint uses) -> checkpoint (ops approves via PATCH /suggestions/{id}).
 * The loop never assigns anything itself — it only ever queues proposals.
 *
 * Runs on {@code AFTER_COMMIT} so it only sees the OFFLINE status once
 * it's durable, and on a dedicated executor so
 * {@code PATCH /agents/{id}/status} never waits on it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReassignmentPlanner {

    private final OrderRepository orderRepository;
    private final SuggestionGenerationService suggestionGenerationService;

    @Async("reassignmentExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAgentWentOffline(AgentWentOfflineEvent event) {
        List<Order> affectedOrders =
                orderRepository.findByAssignedAgent_IdAndStatus(event.agentId(), OrderStatus.ASSIGNED);

        if (affectedOrders.isEmpty()) {
            log.info("Agent {} went offline; no ASSIGNED orders to replan", event.agentId());
            return;
        }

        log.info("Agent {} went offline; replanning {} affected order(s)", event.agentId(), affectedOrders.size());

        for (Order order : affectedOrders) {
            try {
                suggestionGenerationService.generate(
                        order.getId(), RoutingContext.agentOffline(event.agentId(), affectedOrders.size()));
            } catch (RuntimeException ex) {
                // One order's failure (e.g. no agent available) must not abort the rest of the batch.
                log.error("Failed to generate re-plan suggestion for order {} after agent {} went offline",
                        order.getId(), event.agentId(), ex);
            }
        }
    }
}
