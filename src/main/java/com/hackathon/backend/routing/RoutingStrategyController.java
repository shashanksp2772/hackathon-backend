package com.hackathon.backend.routing;

import com.hackathon.backend.routing.dto.RoutingStrategyResponse;
import com.hackathon.backend.routing.dto.UpdateRoutingStrategyRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demoable proof of "switchable at runtime without a restart": flips the
 * {@link RoutingStrategyRegistry}'s active strategy via a live HTTP call.
 */
@RestController
@RequestMapping("/config/routing-strategy")
@RequiredArgsConstructor
@Tag(name = "Routing Strategy", description = "Runtime-switchable active routing strategy (rule-based / ai)")
public class RoutingStrategyController {

    private final RoutingStrategyRegistry registry;

    @GetMapping
    public RoutingStrategyResponse current() {
        return new RoutingStrategyResponse(registry.activeName(), registry.availableNames());
    }

    @PatchMapping
    public RoutingStrategyResponse update(@Valid @RequestBody UpdateRoutingStrategyRequest request) {
        registry.setActive(request.strategy());
        return new RoutingStrategyResponse(registry.activeName(), registry.availableNames());
    }
}
