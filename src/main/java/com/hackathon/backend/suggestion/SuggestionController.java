package com.hackathon.backend.suggestion;

import com.hackathon.backend.suggestion.dto.SuggestionResponse;
import com.hackathon.backend.suggestion.dto.UpdateSuggestionStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/suggestions")
@RequiredArgsConstructor
@Tag(name = "Suggestions", description = "Reassignment suggestions queued for ops approval")
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public List<SuggestionResponse> listSuggestions(@RequestParam(required = false) SuggestionStatus status) {
        return suggestionService.listSuggestions(status);
    }

    @PatchMapping("/{id}")
    public SuggestionResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateSuggestionStatusRequest request) {
        return suggestionService.updateStatus(id, request.status());
    }
}
