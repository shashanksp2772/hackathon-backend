package com.hackathon.backend.order;

import com.hackathon.backend.order.dto.CreateOrderRequest;
import com.hackathon.backend.order.dto.OrderResponse;
import com.hackathon.backend.routing.RoutingContext;
import com.hackathon.backend.suggestion.SuggestionGenerationService;
import com.hackathon.backend.suggestion.dto.SuggestionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle and on-demand reassignment suggestions")
public class OrderController {

    private final OrderService orderService;
    private final SuggestionGenerationService suggestionGenerationService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam(required = false) OrderStatus status) {
        return orderService.listOrders(status);
    }

    @PostMapping("/{id}/suggest")
    public SuggestionResponse suggest(@PathVariable String id) {
        RoutingContext context = orderService.resolveManualSuggestionContext(id);
        return suggestionGenerationService.generate(id, context);
    }
}
