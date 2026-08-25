package com.hackathon.backend.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReassignmentSuggestionRepository extends JpaRepository<ReassignmentSuggestion, UUID> {

    List<ReassignmentSuggestion> findByStatus(SuggestionStatus status);

    Optional<ReassignmentSuggestion> findByOrder_IdAndStatusAndTriggerReason(
            String orderId, SuggestionStatus status, TriggerReason triggerReason);

    List<ReassignmentSuggestion> findByOrder_IdAndTriggerReason(String orderId, TriggerReason triggerReason);
}
