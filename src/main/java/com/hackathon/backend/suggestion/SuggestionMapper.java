package com.hackathon.backend.suggestion;

import com.hackathon.backend.suggestion.dto.SuggestionResponse;

final class SuggestionMapper {

    private SuggestionMapper() {
    }

    static SuggestionResponse toResponse(ReassignmentSuggestion suggestion) {
        return new SuggestionResponse(
                suggestion.getId().toString(),
                suggestion.getOrder().getId(),
                suggestion.getRecommendedAgent().getId(),
                suggestion.getConfidence(),
                suggestion.getReasoning(),
                suggestion.getStatus(),
                suggestion.getTriggerReason(),
                suggestion.getCreatedAt()
        );
    }
}
