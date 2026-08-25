package com.hackathon.backend.suggestion;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.agent.AgentRepository;
import com.hackathon.backend.agent.AgentStatus;
import com.hackathon.backend.common.exception.NoAvailableAgentException;
import com.hackathon.backend.common.exception.ResourceNotFoundException;
import com.hackathon.backend.order.Order;
import com.hackathon.backend.order.OrderRepository;
import com.hackathon.backend.order.OrderStatus;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.routing.RoutingRecommendation;
import com.hackathon.backend.routing.RoutingStrategyRegistry;
import com.hackathon.backend.suggestion.dto.SuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Runs the active routing strategy for one order and persists the result as
 * a {@link ReassignmentSuggestion}. This is the single orchestration point
 * called by both the on-demand HTTP endpoint (T-2) and the async
 * agent-offline listener (T-4) — the strategies themselves stay pure
 * decision logic, and this is where persistence, idempotency, and order
 * state transitions live instead.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionGenerationService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final ReassignmentSuggestionRepository suggestionRepository;
    private final RoutingStrategyRegistry routingStrategyRegistry;

    /**
     * REQUIRES_NEW rather than the default REQUIRED: {@link SuggestionService}
     * calls this from inside its own reject-handling transaction and catches
     * {@link NoAvailableAgentException} to fall back gracefully. Under the
     * default propagation, a RuntimeException crossing this method's
     * transactional proxy marks the *caller's* transaction rollback-only
     * before the catch block ever runs - silently poisoning the reject even
     * though it was handled. Its own transaction means a failure here rolls
     * back only this call, leaving the caller's transaction free to commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SuggestionResponse generate(String orderId, RoutingContext context) {
        if (context.triggerReason() == TriggerReason.AGENT_OFFLINE) {
            var existing = suggestionRepository.findByOrder_IdAndStatusAndTriggerReason(
                    orderId, SuggestionStatus.PENDING, TriggerReason.AGENT_OFFLINE);
            if (existing.isPresent()) {
                log.info("Order {} already has a pending AGENT_OFFLINE suggestion; skipping duplicate", orderId);
                return SuggestionMapper.toResponse(existing.get());
            }
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        List<Agent> availableAgents = agentRepository.findByStatus(AgentStatus.AVAILABLE);

        // Ops already said no to these agents for this order's offline incident.
        // Without this, rejecting just makes SuggestionService immediately ask
        // again, and the deterministic rule strategy hands back the identical
        // top candidate every time - an infinite loop of the same "no".
        if (context.triggerReason() == TriggerReason.AGENT_OFFLINE) {
            Set<String> alreadyRejectedAgentIds = suggestionRepository
                    .findByOrder_IdAndTriggerReason(orderId, TriggerReason.AGENT_OFFLINE)
                    .stream()
                    .filter(s -> s.getStatus() == SuggestionStatus.REJECTED)
                    .map(s -> s.getRecommendedAgent().getId())
                    .collect(Collectors.toSet());
            availableAgents = availableAgents.stream()
                    .filter(agent -> !alreadyRejectedAgentIds.contains(agent.getId()))
                    .toList();
        }

        List<RoutingRecommendation> recommendations =
                routingStrategyRegistry.active().recommend(order, availableAgents, context);

        if (recommendations.isEmpty()) {
            throw new NoAvailableAgentException("No available agent found to recommend for order " + orderId);
        }

        RoutingRecommendation top = recommendations.getFirst();
        Agent recommendedAgent = agentRepository.findById(top.agentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Routing strategy recommended an unknown agent id: " + top.agentId()));

        ReassignmentSuggestion suggestion = ReassignmentSuggestion.builder()
                .id(UUID.randomUUID())
                .order(order)
                .recommendedAgent(recommendedAgent)
                .confidence(top.confidence())
                .reasoning(top.reasoning())
                .status(SuggestionStatus.PENDING)
                .triggerReason(context.triggerReason())
                .createdAt(Instant.now())
                .build();

        if (order.getStatus() != OrderStatus.REASSIGNMENT_PENDING) {
            order.transitionTo(OrderStatus.REASSIGNMENT_PENDING);
        }

        ReassignmentSuggestion saved = suggestionRepository.save(suggestion);
        return SuggestionMapper.toResponse(saved);
    }
}
