package com.hackathon.backend.suggestion;

import com.hackathon.backend.common.exception.InvalidStateTransitionException;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.suggestion.dto.SuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionService {

    private final ReassignmentSuggestionRepository suggestionRepository;
    private final SuggestionStatusTransitioner statusTransitioner;
    private final SuggestionGenerationService suggestionGenerationService;

    @Transactional(readOnly = true)
    public List<SuggestionResponse> listSuggestions(SuggestionStatus status) {
        List<ReassignmentSuggestion> suggestions =
                status != null ? suggestionRepository.findByStatus(status) : suggestionRepository.findAll();
        return suggestions.stream().map(SuggestionMapper::toResponse).toList();
    }

    /**
     * Accepting is a single committed transaction. Rejecting is two
     * sequential ones: the rejection itself commits first (via
     * {@link SuggestionStatusTransitioner}, a separate bean), then - only if
     * this was a recovery suggestion and the original agent is still
     * offline - a fresh recommendation is requested. The ordering matters:
     * {@link SuggestionGenerationService#generate}'s idempotency check reads
     * suggestion state from the database, so it must see the rejection as
     * already committed, not still in-flight in the same transaction that's
     * asking the question.
     */
    public SuggestionResponse updateStatus(UUID suggestionId, SuggestionStatus target) {
        return switch (target) {
            case ACCEPTED -> statusTransitioner.accept(suggestionId);
            case REJECTED -> reject(suggestionId);
            case PENDING -> throw new InvalidStateTransitionException(
                    "Suggestion %s cannot be set back to PENDING".formatted(suggestionId));
        };
    }

    private SuggestionResponse reject(UUID suggestionId) {
        RejectionOutcome outcome = statusTransitioner.reject(suggestionId);

        if (outcome.stillStranded()) {
            try {
                suggestionGenerationService.generate(
                        outcome.orderId(), RoutingContext.agentOffline(outcome.offlineAgentId()));
            } catch (RuntimeException ex) {
                log.warn("No replacement found for order {} after rejection; agent {} is still offline",
                        outcome.orderId(), outcome.offlineAgentId(), ex);
            }
        }

        return outcome.response();
    }
}
