package com.hackathon.backend.suggestion.dto;

import com.hackathon.backend.suggestion.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSuggestionStatusRequest(
        @NotNull(message = "status is required") SuggestionStatus status
) {
}
