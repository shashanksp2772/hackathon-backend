package com.hackathon.backend.agent;

import com.hackathon.backend.agent.dto.AgentResponse;
import com.hackathon.backend.agent.dto.UpdateAgentStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Roster and availability status")
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> listAgents() {
        return agentService.listAgents();
    }

    @PatchMapping("/{id}/status")
    public AgentResponse updateStatus(@PathVariable String id, @Valid @RequestBody UpdateAgentStatusRequest request) {
        return agentService.updateStatus(id, request.status());
    }
}
