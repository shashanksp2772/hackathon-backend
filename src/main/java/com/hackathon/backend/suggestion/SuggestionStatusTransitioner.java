package com.hackathon.backend.suggestion;

import com.hackathon.backend.agent.AgentStatus;
import com.hackathon.backend.common.exception.ResourceNotFoundException;
import com.hackathon.backend.order.Order;
import com.hackathon.backend.order.OrderStatus;
import com.hackathon.backend.suggestion.dto.SuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Applies an accept/reject decision as its own committed transaction.
 * Deliberately a separate bean from {@link SuggestionService}: rejection's
 * "commit the rejection, then decide whether to regenerate" sequencing
 * (see {@link SuggestionService#reject}) needs the rejection to actually
 * commit before the regeneration attempt's idempotency check runs - and a
 * call from one method to another on the *same* bean never crosses
 * Spring's transactional proxy, so this couldn't just be a second
 * {@code @Transactional} method on SuggestionService itself.
 */
@Component
@RequiredArgsConstructor
class SuggestionStatusTransitioner {

    private final ReassignmentSuggestionRepository suggestionRepository;

    @Transactional
    SuggestionResponse accept(UUID suggestionId) {
        ReassignmentSuggestion suggestion = findOrThrow(suggestionId);
        suggestion.accept();
        suggestion.getOrder().reassignTo(suggestion.getRecommendedAgent());
        return SuggestionMapper.toResponse(suggestion);
    }

    @Transactional
    RejectionOutcome reject(UUID suggestionId) {
        ReassignmentSuggestion suggestion = findOrThrow(suggestionId);
        Order order = suggestion.getOrder();

        suggestion.reject();
        order.transitionTo(OrderStatus.ASSIGNED);

        boolean stillStranded = suggestion.getTriggerReason() == TriggerReason.AGENT_OFFLINE
                && order.getAssignedAgent().getStatus() == AgentStatus.OFFLINE;

        return new RejectionOutcome(
                SuggestionMapper.toResponse(suggestion),
                order.getId(),
                order.getAssignedAgent().getId(),
                stillStranded);
    }

    private ReassignmentSuggestion findOrThrow(UUID suggestionId) {
        return suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found: " + suggestionId));
    }
}
