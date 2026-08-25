package com.hackathon.backend.suggestion;

/**
 * What caused this suggestion to be created. Connects the domain model
 * to the agentic loop (T-4) and to the re-plan badge in the UI (T-5).
 */
public enum TriggerReason {
    INITIAL,
    AGENT_OFFLINE
}
