package com.hackathon.backend.routing;

import com.hackathon.backend.common.exception.InvalidRoutingStrategyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Runtime-switchable strategy selection. Spring auto-populates the
 * constructor's {@code Map<String, RoutingStrategy>} with every
 * {@link RoutingStrategy} bean keyed by bean name — so a new strategy
 * registers itself here just by existing as a bean, no wiring changes.
 *
 * The active strategy name is a {@code volatile} field rather than a
 * re-read config property: flipping it via {@link #setActive} takes effect
 * on the very next call, from either caller, with no restart.
 */
@Component
public class RoutingStrategyRegistry {

    private final Map<String, RoutingStrategy> strategiesByName;
    private volatile String activeName;

    public RoutingStrategyRegistry(
            Map<String, RoutingStrategy> strategiesByName,
            @Value("${routing.strategy:rule-based}") String initialStrategyName) {
        this.strategiesByName = Map.copyOf(strategiesByName);
        setActive(initialStrategyName);
    }

    public RoutingStrategy active() {
        return strategiesByName.get(activeName);
    }

    public String activeName() {
        return activeName;
    }

    public Set<String> availableNames() {
        return strategiesByName.keySet();
    }

    public void setActive(String name) {
        if (!strategiesByName.containsKey(name)) {
            throw new InvalidRoutingStrategyException(
                    "Unknown routing strategy '%s'. Available: %s".formatted(name, strategiesByName.keySet()));
        }
        this.activeName = name;
    }
}
