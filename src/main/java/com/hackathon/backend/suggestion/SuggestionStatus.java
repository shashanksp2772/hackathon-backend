package com.hackathon.backend.suggestion;

import java.util.Map;
import java.util.Set;

public enum SuggestionStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    private static final Map<SuggestionStatus, Set<SuggestionStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, Set.of(ACCEPTED, REJECTED),
            ACCEPTED, Set.of(),
            REJECTED, Set.of()
    );

    public boolean canTransitionTo(SuggestionStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
