package com.hackathon.backend.order;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.agent.AgentRepository;
import com.hackathon.backend.agent.AgentStatus;
import com.hackathon.backend.common.exception.AgentUnavailableException;
import com.hackathon.backend.common.exception.ResourceNotFoundException;
import com.hackathon.backend.order.dto.CreateOrderRequest;
import com.hackathon.backend.order.dto.OrderResponse;
import com.hackathon.backend.routing.RoutingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Agent agent = agentRepository.findById(request.assignedAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + request.assignedAgentId()));

        if (agent.getStatus() == AgentStatus.OFFLINE) {
            throw new AgentUnavailableException(
                    "Agent %s is OFFLINE and cannot receive new orders".formatted(agent.getId()));
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .description(request.description())
                .assignedAgent(agent)
                .status(OrderStatus.ASSIGNED)
                .createdAt(Instant.now())
                .build();

        agent.incrementLoad();
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(OrderStatus status) {
        List<Order> orders = status != null ? orderRepository.findByStatus(status) : orderRepository.findAll();
        return orders.stream().map(OrderMapper::toResponse).toList();
    }

    /**
     * A manual "suggest" request on an order stuck ASSIGNED to an OFFLINE
     * agent (the "needs attention" case - every candidate was rejected, or
     * the agentic loop's own attempt failed) is a recovery, not a first
     * assignment: it needs the AGENT_OFFLINE context so the AI strategy's
     * re-plan prompt and ADR-9's rejected-agent exclusion both apply.
     */
    @Transactional(readOnly = true)
    public RoutingContext resolveManualSuggestionContext(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Agent agent = order.getAssignedAgent();
        return agent.getStatus() == AgentStatus.OFFLINE
                ? RoutingContext.agentOffline(agent.getId())
                : RoutingContext.initial();
    }
}
