package com.hackathon.backend.suggestion.dto;

import com.hackathon.backend.suggestion.SuggestionStatus;
import com.hackathon.backend.suggestion.TriggerReason;

import java.time.Instant;

public record SuggestionResponse(
        String id,
        String orderId,
        String recommendedAgentId,
        double confidence,
        String reasoning,
        SuggestionStatus status,
        TriggerReason triggerReason,
        Instant createdAt
) {
}
