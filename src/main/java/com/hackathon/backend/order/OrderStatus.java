package com.hackathon.backend.order;

import java.util.Map;
import java.util.Set;

/**
 * ASSIGNED -> REASSIGNMENT_PENDING -> REASSIGNED -> DELIVERED, with
 * REASSIGNMENT_PENDING able to fall back to ASSIGNED on a rejected
 * suggestion. The allowed-transition table is the single source of
 * truth for validity, so adding a future status is a one-line change
 * here rather than logic scattered across services.
 */
public enum OrderStatus {
    ASSIGNED,
    REASSIGNMENT_PENDING,
    REASSIGNED,
    DELIVERED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            ASSIGNED, Set.of(REASSIGNMENT_PENDING),
            REASSIGNMENT_PENDING, Set.of(REASSIGNED, ASSIGNED),
            REASSIGNED, Set.of(REASSIGNMENT_PENDING, DELIVERED),
            DELIVERED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
