package com.hackathon.backend.agent.dto;

import com.hackathon.backend.agent.AgentStatus;

public record AgentResponse(
        String id,
        String name,
        AgentStatus status,
        int activeOrderCount,
        String currentZone,
        Integer maxCapacity
) {
}
