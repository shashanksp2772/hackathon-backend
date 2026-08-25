package com.hackathon.backend.agent.dto;

import com.hackathon.backend.agent.AgentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAgentStatusRequest(
        @NotNull(message = "status is required") AgentStatus status
) {
}
