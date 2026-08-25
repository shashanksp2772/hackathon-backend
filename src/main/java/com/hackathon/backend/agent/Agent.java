package com.hackathon.backend.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A delivery agent. Load tracking ({@code activeOrderCount}) is maintained
 * here rather than derived with a live count query, so routing strategies
 * can read it off an in-memory snapshot without hitting the order table.
 */
@Entity
@Table(name = "agents")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status;

    @Column(name = "active_order_count", nullable = false)
    private int activeOrderCount;

    @Column(name = "current_zone", length = 50)
    private String currentZone;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    public void updateStatus(AgentStatus newStatus) {
        this.status = newStatus;
    }

    public void incrementLoad() {
        this.activeOrderCount++;
    }

    public void decrementLoad() {
        this.activeOrderCount = Math.max(0, this.activeOrderCount - 1);
    }
}
