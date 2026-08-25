package com.hackathon.backend.order.dto;

import com.hackathon.backend.order.OrderStatus;

import java.time.Instant;

public record OrderResponse(
        String id,
        String description,
        String assignedAgentId,
        OrderStatus status,
        Instant createdAt,
        String pickupZone,
        String dropoffZone,
        String weightClass,
        Instant slaDeadline
) {
}
