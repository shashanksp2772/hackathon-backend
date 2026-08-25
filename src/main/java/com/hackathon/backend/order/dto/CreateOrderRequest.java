package com.hackathon.backend.order.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "description is required") String description,
        @NotBlank(message = "assignedAgentId is required") String assignedAgentId
) {
}
