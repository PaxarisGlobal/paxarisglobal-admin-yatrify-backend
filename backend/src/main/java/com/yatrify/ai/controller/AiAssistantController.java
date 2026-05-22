package com.yatrify.ai.controller;

import com.yatrify.ai.dto.AiChatRequest;
import com.yatrify.ai.dto.AiChatResponse;
import com.yatrify.ai.service.AiAssistantService;
import com.yatrify.common.ApiResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Yatri AI assistant for trip planning and booking help")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat/guest")
    @Operation(summary = "Chat with AI assistant (no auth required)")
    public Mono<ResponseEntity<ApiResponse<AiChatResponse>>> guestChat(
            @Valid @RequestBody AiChatRequest request) {
        return aiAssistantService.chat(request, false)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    @PostMapping("/chat/user")
    @Operation(summary = "Chat with AI assistant as authenticated user")
    public Mono<ResponseEntity<ApiResponse<AiChatResponse>>> userChat(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody AiChatRequest request) {
        return aiAssistantService.chat(request, false)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    @PostMapping("/chat/organizer")
    @Operation(summary = "Chat with AI assistant as organizer for trip planning")
    public Mono<ResponseEntity<ApiResponse<AiChatResponse>>> organizerChat(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody AiChatRequest request) {
        return aiAssistantService.chat(request, true)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    @PostMapping("/generate-itinerary")
    @Operation(summary = "Generate trip itinerary using AI")
    public Mono<ResponseEntity<ApiResponse<AiChatResponse>>> generateItinerary(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @RequestBody Map<String, String> request) {
        String destination = request.get("destination");
        int days = Integer.parseInt(request.getOrDefault("days", "5"));
        String tripType = request.getOrDefault("tripType", "family");
        String preferences = request.getOrDefault("preferences", "");

        return aiAssistantService.generateTripItinerary(destination, days, tripType, preferences)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }
}
