package com.hackathon.backend.order;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.common.exception.InvalidStateTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id", nullable = false)
    private Agent assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "pickup_zone", length = 50)
    private String pickupZone;

    @Column(name = "dropoff_zone", length = 50)
    private String dropoffZone;

    @Column(name = "weight_class", length = 20)
    private String weightClass;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    public void transitionTo(OrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "Order %s cannot transition from %s to %s".formatted(id, status, target));
        }
        this.status = target;
    }

    /**
     * Applies an accepted reassignment: moves the order's load off the
     * previous agent and onto the new one, then marks the order REASSIGNED.
     */
    public void reassignTo(Agent newAgent) {
        this.assignedAgent.decrementLoad();
        newAgent.incrementLoad();
        this.assignedAgent = newAgent;
        transitionTo(OrderStatus.REASSIGNED);
    }
}
