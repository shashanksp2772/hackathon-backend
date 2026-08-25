package com.hackathon.backend.order;

import com.hackathon.backend.order.dto.OrderResponse;

final class OrderMapper {

    private OrderMapper() {
    }

    static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                order.getAssignedAgent().getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getPickupZone(),
                order.getDropoffZone(),
                order.getWeightClass(),
                order.getSlaDeadline()
        );
    }
}
