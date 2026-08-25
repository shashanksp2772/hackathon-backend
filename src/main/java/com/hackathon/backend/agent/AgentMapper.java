package com.hackathon.backend.agent;

import com.hackathon.backend.agent.dto.AgentResponse;

final class AgentMapper {

    private AgentMapper() {
    }

    static AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getStatus(),
                agent.getActiveOrderCount(),
                agent.getCurrentZone(),
                agent.getMaxCapacity()
        );
    }
}
