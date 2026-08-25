package com.hackathon.backend.suggestion;

import com.hackathon.backend.suggestion.dto.SuggestionResponse;

/** What a committed rejection leaves behind, for {@link SuggestionService} to act on. */
record RejectionOutcome(SuggestionResponse response, String orderId, String offlineAgentId, boolean stillStranded) {
}
