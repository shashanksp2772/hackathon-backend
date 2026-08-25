package com.hackathon.backend.agent;

/**
 * Published exactly once per genuine AVAILABLE/BUSY -> OFFLINE transition
 * (not on redundant OFFLINE -> OFFLINE PATCHes). Consumed asynchronously by
 * the agentic re-planning loop.
 */
public record AgentWentOfflineEvent(String agentId) {
}
