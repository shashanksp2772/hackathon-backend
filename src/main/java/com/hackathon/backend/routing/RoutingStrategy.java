package com.hackathon.backend.routing;

import com.hackathon.backend.agent.Agent;
import com.hackathon.backend.order.Order;

import java.util.List;

/**
 * The routing contract. Deliberately free of persistence and I/O concerns —
 * implementations just rank candidate agents for an order. Orchestration
 * (loading agents, calling the active strategy, persisting the result) lives
 * one layer up, so strategies stay trivially unit-testable and swappable.
 *
 * Adding a new strategy (e.g. sprint 2's ZoneAffinityStrategy) means
 * implementing this interface and registering it as a named Spring bean —
 * no changes here, in {@link RoutingStrategyRegistry}, or in either caller.
 */
public interface RoutingStrategy {

    List<RoutingRecommendation> recommend(Order order, List<Agent> availableAgents, RoutingContext context);
}
