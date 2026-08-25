package com.hackathon.backend.agent;

import com.hackathon.backend.agent.dto.AgentResponse;
import com.hackathon.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AgentResponse> listAgents() {
        return agentRepository.findAll().stream()
                .map(AgentMapper::toResponse)
                .toList();
    }

    /**
     * Publishes {@link AgentWentOfflineEvent} only on a genuine transition
     * into OFFLINE, not on a redundant OFFLINE -> OFFLINE PATCH — the
     * agentic loop should fire because something changed, not because
     * ops re-sent the same status.
     */
    @Transactional
    public AgentResponse updateStatus(String agentId, AgentStatus newStatus) {
        Agent agent = findAgentOrThrow(agentId);
        AgentStatus previousStatus = agent.getStatus();
        agent.updateStatus(newStatus);

        if (newStatus == AgentStatus.OFFLINE && previousStatus != AgentStatus.OFFLINE) {
            eventPublisher.publishEvent(new AgentWentOfflineEvent(agentId));
        }

        return AgentMapper.toResponse(agent);
    }

    private Agent findAgentOrThrow(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));
    }
}
