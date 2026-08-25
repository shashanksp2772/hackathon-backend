package com.hackathon.backend.suggestion;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.common.exception.InvalidStateTransitionException;
import com.hackathon.backend.order.Order;
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
import java.util.UUID;

@Entity
@Table(name = "reassignment_suggestions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignmentSuggestion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_agent_id", nullable = false)
    private Agent recommendedAgent;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false, columnDefinition = "text")
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false, length = 20)
    private TriggerReason triggerReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public void accept() {
        transitionTo(SuggestionStatus.ACCEPTED);
    }

    public void reject() {
        transitionTo(SuggestionStatus.REJECTED);
    }

    private void transitionTo(SuggestionStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "Suggestion %s cannot transition from %s to %s".formatted(id, status, target));
        }
        this.status = target;
    }
}
