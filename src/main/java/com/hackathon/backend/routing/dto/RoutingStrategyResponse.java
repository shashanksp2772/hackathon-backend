package com.hackathon.backend.routing.dto;

import java.util.Set;

public record RoutingStrategyResponse(String active, Set<String> available) {
}
